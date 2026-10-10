# Lost-passport demo and routing regression

## Scope

The user asked for a generic Jordanian lost-passport request to reach documented guidance, then selected verification of the existing birth-certificate, CSPD-document certification, declared-address and family-book cases before expanding further.

The app now includes the ordinary Jordanian passport replacement card from the supplied official CSPD guide, PDF pages 48–49. Both pages and the workflow diagram were visually reviewed. The PDF SHA256 is `78f69da93f19e8896c73a7a7d999ed5b7b6e8c6f3d4eaea720c2ad449cf87c23`. These are historical 2024 instructions; current fees and rules are not independently verified.

## Presentation rehearsal

1. Say: **جواز السفر تبعي ضاع، شو أسوي؟**
2. The assistant asks whether the passport is ordinary Jordanian, temporary or another type. It must not repeat the CSPD-document-origin question.
3. Reply: **جواز أردني عادي**.
4. The app presents 11 cards: preparation, public-service intake, employee acceptance, investigation/security approval and guarantee determination, approval letter, transaction verification, authorization, payment, printing, lamination and collection.

A clear opening such as **أنا أردني وجواز سفري الأردني العادي ضاع داخل الأردن لأول مرة. شو لازم أعمل؟** goes directly to the cards.

Fees stay conditional: 125 JOD for first loss, 250 JOD for subsequent loss, and collection of the earlier guarantee if the prior loss was less than five years ago. The 50–500 JOD guarantee is separate from issuance fees; the committee determines its amount. Do not present both first and subsequent loss fees as simultaneously payable.

Documents for minors, military obligations, bridge cards and loss outside Jordan appear as conditional requirements. No photo dimensions are invented. Committee approval and guarantee determination occur after intake; they are not falsely required before beginning the application. The guide names the security centre and Ministry of Justice but does not supply their complete external procedures. Embassy execution and electronic passport steps are not claimed by these in-person cards. The printed processing duration is not presented as total end-to-end time.

## Runtime safeguards

OpenRouter classifies six bounded issue fields. It does not author the procedural cards. The catalog, unchanged JSON response schema and semantic validator approve only the reviewed plan. Generic passport queries ask only the passport-type prerequisite. Temporary or unknown types stop with an apology rather than receiving the ordinary-passport plan.

A new issue clears previous facts and pending questions. Synthetic classifier phrases were removed, so unrelated unsupported requests no longer acquire the word “passport.” Switching services detaches the previous saved operation rather than overwriting it. Restored supported plans are retained for all approved services. Suggested-response UI remains disabled. Credentials remain in ignored local files only.

## Verification

The opt-in `ThreeServiceLiveTest` uses the actual OpenRouter gateway, catalog, response codec and semantic validator. It covers a clear passport request and a continuous conversation from document certification to unsupported education certification, generic passport clarification and answer, birth certificate, CSPD-document certification, declared address and lost family book. Local tests check missing prerequisites, unsupported branches and rejection of fabricated procedures.

All 9 tests passed with no skips or failures (12 live API calls). Results and accepted responses are recorded under `evidence/passport-routing/`. These are JVM integration tests, not an observed Android UI or microphone walkthrough. The family-book plan remains the previously reviewed partial electronic procedure; no claim of complete family-book coverage is added.
