# Grounding sources and evidence boundaries

Research access date: 2026-10-09. These are source leads with recorded inspection, not an assertion that every current rule has been verified.

## Pilot references

| ID | Reference | What is established |
| --- | --- | --- |
| cspd-lost-family-book-electronic | [CSPD electronic lost-family-book card](https://cspd.gov.jo/AR/ListDetails/الخدمات_الالكترونية/42/10) | Body read. Supports the selected pilot and identifies an electronic route, a police-report prerequisite and conditional follow-up. No portrait listed. Substantive revision date and actual execution unverified. |
| cspd-guide-2024 | [Open-data directory dataset/API guide](https://opendata.gov.jo/ar/dataset/api_guide/services-directory-2620-2024) | Official PDF resource matches the supplied 2024 guide. Historical CSPD reference; 162 pages, 67 entries, 13 chapters. Lost-book cards/pages 28–29 and 162 compared; page 29 visually inspected. |
| cspd-service-index | [CSPD service directory](https://www.cspd.gov.jo/AR/List/_دليل__الخدمات) | Listing inspected; useful complementary cards exist. Not all service variants reviewed. |

The user's PDF is historical. Image workflows and multi-column conditions make unreviewed text extraction unreliable. Current and historical time/delivery details differ; preserve versions rather than blending their numbers. The guide is not all-government coverage and does not justify unrestricted answers to every civil-service question.

## Open-data API observations

`GET https://opendata.gov.jo/api/3/action/package_show?id=services-directory-2620-2024` returned HTTP 200 without an account/key. This dataset had one PDF resource and `datastore_active: false`. Official file SHA256:

`78f69da93f19e8896c73a7a7d999ed5b7b6e8c6f3d4eaea720c2ad449cf87c23`

Catalog search GET returned HTTP 400 requesting POST; a read-only POST with JSON search parameters succeeded. This is catalog metadata/resource access, not application submission, identity access, booking, personal reminders or a transaction-status API. These research observations do not add a live open-data API call to the current AI-only app scope.

## Location dependency

No complete verified service-centre/shop catalog or live availability API was established. The team must curate and version relevant pilot entries before treating them as real recommendations. A place record needs an ID, name/type, service capabilities, coordinates or an explicit location gap, approved maps link, source/date and availability basis. The AI must not invent a location to satisfy a count of three.

## Differentiation boundary

[MoDEE AI implementation plan](https://www.modee.gov.jo/EBV4.0/Root_Storage/EN/husam/Artificial_Intelegence_implmentation_plan_project_cards.pdf), project 30 / PDF page 31, publicly proposes conversational/voice Sanad features and prefilling. The extracted project card was read; deployment is unknown. Present DALEELAK's demonstrated workflow and intended benefit; do not claim the broad category has never been announced or that an existing feature is absent without evidence.

No firsthand adoption study, measured visit reduction, production integration or government partnership was established. User-reported mentor permission allows labeled simulated integrations for the hackathon, subject to institutional feasibility; it does not grant protected API access.
