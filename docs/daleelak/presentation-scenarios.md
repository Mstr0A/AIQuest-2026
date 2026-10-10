# Presentation scenarios and actual AI flow

> **Current scope:** Three separate reviewed 2024 services are now implemented: registered-birth certificate, CSPD-document certification and declared-address update. See [three-service demo](three-service-demo.md). Earlier lost-book-only prototype scenarios below are historical; the deployed classifier now accepts five bounded fact fields.


## Source coverage

Three government references are recorded in sources.md: the 2024 CSPD directory PDF, the CSPD service index, and the electronic lost-family-book card. They are not three complete service datasets loaded into the app. Only a narrow inspection summary of the lost-book card is currently bundled. The preserved PDF has a broader service index, but its procedures have not been turned into reviewed app rules. Expanding coverage requires exact excerpt review, condition/dependency extraction and updated catalog/validation.

## Four additional direct tests

All four passed schema and reviewed-content checks. Reported cost: $0.00212744. Evidence: evidence/assistant-four-more-evaluation.json. These are model prototype tests, not new Android walkthroughs.

1. **The user does not name the transaction.** Say: “بدي أخلص معاملة حكومية ومش عارف من وين أبدأ.” Expected: clarify the issue before providing instructions. No user profile interview.
2. **Everything needed is in the first message.** Say: “ضاع دفتر العيلة، ولسه ما عندي بلاغ من الشرطة. أعطيني الخطوات اللي بتعرفها.” Expected: return the partial plan immediately, without asking whether a report exists again. Start with the report prerequisite; its filing method remains unknown.
3. **The user corrects an earlier answer.** Previously say a report exists, then: “تصحيح: قلت عندي بلاغ بس اكتشفت إنه مش بلاغ فقدان. فعلياً ما عندي بلاغ من الشرطة.” Expected: update the report fact to no and change the immediate recommendation. This does not independently verify the report or complete/reset saved steps.
4. **Similar documents need different procedures.** Say: “دفتر العيلة موجود معي، بس تالف من المي، مش ضايع. كيف أبدله؟” Expected: recognize damaged, not lost, and disclose that this procedure is outside the current reviewed coverage. Do not present the lost-book steps.

Manual review found the expected next actions and coverage explanations without invented fees, locations or filing requirements. Broader fluency and factual correctness are not established by four examples.

## Flow is conditional

There is no mandatory ask–clarify–plan sequence. Interpret the message and reuse known issue facts. If a necessary issue fact is missing, ask the approved question. If sufficient facts are already present, construct the reviewed partial plan immediately. If the service is outside reviewed coverage, explain the limit. Missing source rules remain explicit gaps; more personal questions do not solve them.

The current Android model extracts issue facts; local catalog logic constructs the response. JSON/schema and reviewed-content validation precede display. The presenter rebuilds procedural prose. A plan response sets currentPlan; the user can view/save the plan and access the journey cards without an obligatory clarification turn. The separately evaluated full-envelope prompt follows the same branch decisions but is not deployed.

A clear request therefore skips clarification, not validation. It still cannot obtain instructions absent from the reviewed sources. Direct prototype success does not prove that every deployed classifier/local matcher combination handles the same wording; rehearse the actual app before presenting.

## Main limitation in simple terms

We have more documents than the app currently understands. The bottleneck is converting those documents into trustworthy, complete service rules and loading them into the app. The four new tests demonstrate conversation behavior, not four additional supported government services.
