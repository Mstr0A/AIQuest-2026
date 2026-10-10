# Three separate service topics — current demo

These are three different government procedures added to the app, not three branches of lost-family-book dialogue. All use visually reviewed pages and workflow diagrams from the supplied official **2024** CSPD directory. Their continued current validity is not established. The app displays that version limit and does not execute government transactions.

## 1. Obtain a birth certificate for an already registered birth

Say: **أنا أردني بالغ وبدي شهادة ولادة لنفسي، والواقعة مسجلة حاسوبياً لدى الأحوال المدنية. كيف أطلع الشهادة؟**

Expected six cards:

1. Prepare the documents and choose Arabic or English for the certificate. The guide lists the applicant's Jordanian ID and the family book or national-number ID; documents for non-Jordanians and an old certificate are conditional, not universal.
2. Visit public service, present the documents and obtain a queue number.
3. Staff compare the information and accept the request on the system.
4. Pay the certificate fee stated in the 2024 guide (1 dinar) and retain the receipt.
5. Staff send the request for printing.
6. Collect the stamped certificate after presenting the receipt.

Source: PDF pages 16–17. This issues proof of an existing birth record; it does **not** register a new birth. If computerized registration is not stated, ask the approved registration question. An unregistered or uncertain case does not receive this happy-path plan; the guide mentions referral to the office holding the original record, whose complete procedure is not reviewed here.

## 2. Certify a copy of a CSPD-issued document

Say: **بدي أصدق صورة شهادة الزواج الصادرة عن دائرة الأحوال المدنية والجوازات. معي الأصل والصورة وهويتي.**

Expected five cards:

1. Prepare the original document, a clear photocopy on copying paper and your personal ID. A fax copy is not accepted in the guide.
2. Visit a CSPD public-service office with the originals and copies.
3. Submit the document and copy to the information employee.
4. Staff compare the copy against the document.
5. Collect the certified document; confirm the stamp requirement at the office.

Source: PDF page 130. This covers documents issued by CSPD, in Arabic or English, and their copies. It excludes translations issued by translation institutes. If the issuing agency is unclear, ask before selecting this route.

**Source discrepancy:** the fees section says no fees, while the diagram mentions service stamps worth 250 fils. The app preserves both and asks for confirmation rather than asserting a payment total.

## 3. Update the declared address for official notices

Say: **أنا رب الأسرة وبدي أحدث العنوان المصرح به للتبليغات بعد تغيير عنواني. شو الخطوات؟**

Expected four cards:

1. Prepare the declared-address form and the declaration from the head of household, spouse or representative. The request is submitted by the head of household or representative.
2. Visit public service, complete the address form and obtain a queue number.
3. Staff enter the address in the declared-address database.
4. Staff save and archive the transaction.

Source: PDF pages 132–133. This address is for judicial, administrative and financial notices. It does **not** change the residence shown on identity documents. Official-letter filing in the diagram concerns government-agency requests, not an additional requirement imposed on this citizen scenario.

The guide states reporting within 30 days of an address change and a 10-dinar penalty after that period. The app identifies this as a historical rule and does not infer that the user owes a fine.

## Actual app flow

The live OpenRouter classifier extracts only bounded issue facts: `issue`, `police_report`, `birth_registered`, `document_origin`, `address_authority`. Local reviewed catalogs select the applicable exact question or step plan. JSON/schema and service-specific semantic validation run before display; the presenter uses approved text rather than arbitrary model procedures.

A clear message can produce a plan immediately. Missing necessary facts trigger a reviewed question. Explicitly unsupported branches disclose the limit. The app displays a plan preview, and **حفظ الخطة ومتابعة الخطوات** opens the journey cards. Existing lost-family-book partial guidance is preserved.

## Key and UI

The initial demo key was published with user authorization in commit `b261733`, then started returning HTTP 401. User U-064 now requires replacement keys to stay outside GitHub. The real `android/demo-secrets.properties` file is untracked and ignored; copy the supplied `.example` file and set `OPENROUTER_DEMO_KEY` locally. Debug BuildConfig embeds that local value into the APK. Release BuildConfig remains empty. The old key remains in historical commits; no history rewrite was requested. Do not print a replacement key in handoffs or test logs.

Three live service scenarios succeeded before publication. The latest live rerun failed because the provider rejected the credential; the four local grounding/regression checks passed. A replacement was supplied privately and the final rebuild/live rerun passed all seven checks.
The AI key-settings button/dialog is removed. New chat appears only after a user message exists. The embedded debug key activates classification, STT and TTS without manual entry. Voice remains editable before sending, and playback/mute controls remain.

## Verification

`ThreeServiceLiveTest` calls the real `OpenRouterAssistant` directly from JVM tests using the embedded debug credential, then runs the actual catalog, codec, semantic validator and domain mapping. Three distinct service messages returned their exact reviewed plans without clarification. Additional local checks cover missing-fact questions, unsupported prerequisite branches, fabricated procedure rejection and the legacy lost-book plan. Evidence is under `evidence/three-services/`. The accepted response files and final-tests.xml come from the successful replacement-key rerun; credential-status.json records the earlier HTTP 401 from the original key.

Run explicitly paid tests:

```sh
DALEELAK_ALLOW_PAID_SERVICE_TESTS=true bash tools/android/build-local.sh :app:testDebugUnitTest --tests com.a0.daleelak.ThreeServiceLiveTest :app:assembleDebug
```

The Android UI was not walked through in this turn: no phone was visible, Waydroid was stopped and sandbox session startup returned Invalid session pid. The user canceled the install request and asked to continue. Compilation and API/app-logic checks do not establish on-device UI behavior or actual government completion.
