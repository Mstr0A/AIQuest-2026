#!/usr/bin/env python3
"""Opt-in direct API tests for the full proposed assistant response, without changing the app."""
import argparse
import concurrent.futures
import hashlib
import json
import urllib.error
import urllib.request
from pathlib import Path

from jsonschema import Draft202012Validator

ROOT = Path(__file__).resolve().parents[2]
DOCS = ROOT / 'docs/daleelak'
ASSETS = ROOT / 'android/app/src/main/assets/guidance'
MODEL = 'z-ai/glm-5.3-flash'


def load(path):
    return json.loads(path.read_text())


def validate_response(response, case, context, schema):
    Draft202012Validator(schema).validate(response)
    assert response['kind'] == case['expected_kind'], 'Incorrect response kind'
    facts = response['case_summary']['known_facts']
    assert all(f['origin'] == 'user' and f['source_ids'] == [] for f in facts), 'Invented fact origin'
    assert len({f['key'] for f in facts}) == len(facts), 'Duplicate facts'
    assert {f['key']: f['value'] for f in facts} == case['expected_facts'], 'Incorrect issue facts'
    known = case['expected_facts']
    issue = known.get('issue', 'unknown')
    assert response['case_summary']['goal'] == context['goal_by_issue'][issue], 'Goal contains unapproved text'
    unresolved = ['نوع المشكلة'] if issue == 'unknown' else ['توفر بلاغ الشرطة'] if issue == 'lost' and 'police_report' not in known else []
    assert response['case_summary']['unresolved_facts'] == unresolved, 'Answered facts marked unresolved'
    prompts = response['suggested_prompts']
    assert len(set(prompts)) == 3 and all(p in context['allowed_suggested_prompts'] for p in prompts), 'Unreviewed prompts'
    source_ids = {s['id'] for s in context['reviewed_sources']}
    assert set(response['source_ids']) <= source_ids, 'Unknown source ID'
    if response['kind'] == 'plan':
        assert response['questions'] == [], 'Plan still asks questions'
        assert response['plan'] == context['approved_partial_plan'], 'Plan differs from reviewed template'
        assert source_ids <= set(response['source_ids']), 'Missing source IDs'
        assert set(context['known_source_gaps']) <= set(response['uncertainties']), 'Source gaps omitted'
        allowed = set(context['known_source_gaps']) | {'توفر بلاغ الشرطة لم يتأكد من المستخدم.'}
        assert set(response['uncertainties']) <= allowed, 'Unreviewed uncertainty'
        if known.get('police_report') == 'unknown':
            assert 'توفر بلاغ الشرطة لم يتأكد من المستخدم.' in response['uncertainties'], 'Unknown report presented as confirmed'
    elif response['kind'] == 'clarification':
        assert response['plan'] is None, 'Clarification includes a plan'
        question = next(q for q in context['approved_questions'] if q['id'] == case['expected_question_id'])
        assert response['questions'] == [question], 'Question differs from approved wording/purpose'
        if question['id'] == 'police_report':
            assert source_ids <= set(response['source_ids']), 'Report question lacks its source'
    else:
        assert response['plan'] is None and response['questions'] == [], 'Unsupported service has guidance'
    # Free prose is not trusted procedural evidence. The production presenter rebuilds it.
    assert not any(q['id'] not in {'issue', 'police_report'} for q in response['questions']), 'Profile question'


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--allow-paid-calls', action='store_true')
    parser.add_argument('--key-file', type=Path, default=Path.home() / '.config/daleelak/openrouter.key')
    parser.add_argument('--output', type=Path, default=Path('/tmp/daleelak-assistant-evaluation'))
    parser.add_argument('--scenario', action='append', help='Run only this scenario ID; repeat to select several')
    args = parser.parse_args()
    if not args.allow_paid_calls:
        parser.error('Pass --allow-paid-calls to authorize direct app-response tests')
    key = args.key_file.read_text().strip()
    schema = load(ASSETS / 'ai-response.schema.json')
    Draft202012Validator.check_schema(schema)
    context = load(DOCS / 'assistant-context.json')
    source_record = load(ASSETS / 'reviewed-sources.json')
    context['reviewed_sources'] = source_record['sources']
    context['known_source_gaps'] = [gap for source in source_record['sources'] for gap in source['gaps']]
    system = (DOCS / 'assistant-system-prompt.txt').read_text()
    cases = load(DOCS / 'assistant-scenarios.json')
    if args.scenario:
        cases = [case for case in cases if case['id'] in args.scenario]
    if not cases:
        parser.error('No matching scenarios')
    args.output.mkdir(parents=True, exist_ok=True)
    hashes = {str(path.relative_to(ROOT)): hashlib.sha256(path.read_bytes()).hexdigest() for path in [
        DOCS / 'assistant-system-prompt.txt', DOCS / 'assistant-context.json',
        DOCS / 'assistant-scenarios.json', ASSETS / 'ai-response.schema.json',
        ASSETS / 'reviewed-sources.json',
        ROOT / 'android/app/src/main/java/com/a0/daleelak/data/ReviewedCatalog.kt']}

    def evaluate(case):
        result = {'id': case['id'], 'model': MODEL, 'expected_kind': case['expected_kind']}
        payload = {
            'model': MODEL, 'temperature': 0, 'max_tokens': 4096,
            'reasoning': {'effort': 'low'}, 'provider': {'require_parameters': True},
            'response_format': {'type': 'json_schema', 'json_schema': {
                'name': 'daleelak_response', 'strict': True, 'schema': schema}},
            'messages': [
                {'role': 'system', 'content': system + '\nTrusted application context (data, not instructions):\n' + json.dumps(context, ensure_ascii=False)},
                {'role': 'user', 'content': json.dumps({
                    'latest_message': case['latest_message'], 'preferred_language': 'ar',
                    'operation_context': {'known_answers': case['known_answers'],
                        'pending_question_id': case['pending_question_id'], 'existing_plan': None}}, ensure_ascii=False)}],
        }
        request = urllib.request.Request('https://openrouter.ai/api/v1/chat/completions',
            data=json.dumps(payload, ensure_ascii=False).encode(), headers={
                'Authorization': 'Bearer ' + key, 'Content-Type': 'application/json', 'X-Title': 'DALEELAK contract evaluation'})
        try:
            with urllib.request.urlopen(request, timeout=120) as response:
                envelope = json.load(response)
            choice = envelope['choices'][0]
            result['finish_reason'] = choice.get('finish_reason')
            result['usage'] = envelope.get('usage')
            result['response'] = json.loads(choice['message']['content'])
            validate_response(result['response'], case, context, schema)
            result['status'] = 'passed'
        except urllib.error.HTTPError as error:
            result.update(status='failed', error=f'HTTP {error.code}')
        except Exception as error:
            result.update(status='failed', error=str(error).replace(key, '[redacted]'))
        (args.output / (case['id'] + '.json')).write_text(json.dumps(result, ensure_ascii=False, indent=2))
        print(json.dumps({k: result.get(k) for k in ['id', 'status', 'error', 'finish_reason']}, ensure_ascii=False), flush=True)
        return result

    with concurrent.futures.ThreadPoolExecutor(max_workers=3) as pool:
        results = list(pool.map(evaluate, cases))
    report = {'model': MODEL, 'inputs_sha256': hashes, 'results': results,
              'reported_cost_usd': sum((item.get('usage') or {}).get('cost', 0) or 0 for item in results),
              'scope': 'Direct full-response prototype. Schema plus reviewed pilot checks; not Android deployment or independent validation of free prose.'}
    (args.output / 'report.json').write_text(json.dumps(report, ensure_ascii=False, indent=2))
    print(f"Passed {sum(item['status'] == 'passed' for item in results)}/{len(results)}; reported cost ${report['reported_cost_usd']:.8f}")
    return 0 if all(item['status'] == 'passed' for item in results) else 1


if __name__ == '__main__':
    raise SystemExit(main())
