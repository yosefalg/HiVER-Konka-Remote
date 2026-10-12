# HiVER / Konka Consumer IR Remote

تطبيق Android محلي لإرسال إشارات **Consumer IR حقيقية** عبر مرسل الأشعة تحت الحمراء المدمج في الهاتف. لا يستخدم الإنترنت أو الإعلانات أو مفاتيح API.

## ملفات التحكم المتاحة

الملفات التالية مبنية على خرائط مفاتيح وبروتوكولات منشورة في مصدر MediaTek/TCL MT9653، وليست أكواداً مخمّنة:

- **Konka STAOS** — بروتوكول Konka، customer code ‏`0x0002`، مع الاتجاهات وOK والأرقام والأزرار الملونة.
- **Changhong TV** — NEC customer code ‏`0x40BF`.
- **Hisense TV** — NEC customer code ‏`0x00BF`.
- **MediaTek Smart TV** — NEC customer code ‏`0x807F`.

اختيار الملف متاح من قائمة «الأجهزة»، و«ريموت جوكر» يختبر زر التشغيل لكل ملف قبل اعتماده. تُخفى مجموعة الاتجاهات عندما لا يحتوي الملف على أكواد الاتجاهات وOK؛ ولا تظهر اختصارات غير موجودة في خريطة الملف.

## توافق HiVER H43F01 / RID 5040

لم نعثر في المصدر العام المستخدم هنا على ملف يثبت تطابقاً مع **HiVER H43F01 / Welife RID 5040**. لذلك لا يدّعي التطبيق أن أي ملف أعلاه هو الريموت الأصلي. الملفات خيارات اختبار مبنية على مصادر عامة لأجهزة أخرى، ويجب تأكيد الاستجابة من التلفاز نفسه. الهاتف يرسل الإشارة ولا يملك مستقبلاً لقياس ما إذا كان التلفاز استجاب.

## مصادر الأكواد والتوقيت

المصدر: [TCLOpenSource/mt9653 — snapshot pinned to commit 85db067](https://github.com/TCLOpenSource/mt9653/tree/85db067ea371bb87753370ab99ced1e1f8ced3d8)

- [Konka STAOS keymap](https://github.com/TCLOpenSource/mt9653/blob/85db067ea371bb87753370ab99ced1e1f8ced3d8/drivers/input/keyboard/mtk_ir/keymaps/keymap-konka-tv.c)
- [Changhong keymap](https://github.com/TCLOpenSource/mt9653/blob/85db067ea371bb87753370ab99ced1e1f8ced3d8/drivers/input/keyboard/mtk_ir/keymaps/keymap-changhong-tv.c)
- [Hisense keymap](https://github.com/TCLOpenSource/mt9653/blob/85db067ea371bb87753370ab99ced1e1f8ced3d8/drivers/input/keyboard/mtk_ir/keymaps/keymap-hisense-tv.c)
- [MediaTek TV keymap](https://github.com/TCLOpenSource/mt9653/blob/85db067ea371bb87753370ab99ced1e1f8ced3d8/drivers/input/keyboard/mtk_ir/keymaps/keymap-mtk-tv.c)
- [Konka protocol decoder](https://github.com/TCLOpenSource/mt9653/blob/85db067ea371bb87753370ab99ced1e1f8ced3d8/drivers/input/keyboard/mtk_ir/protocols/ir-konka-decoder.c)
- [NEC protocol decoder](https://github.com/TCLOpenSource/mt9653/blob/85db067ea371bb87753370ab99ced1e1f8ced3d8/drivers/input/keyboard/mtk_ir/protocols/ir-nec-decoder.c)
- [Protocol timing definitions and customer codes](https://github.com/TCLOpenSource/mt9653/blob/85db067ea371bb87753370ab99ced1e1f8ced3d8/drivers/input/keyboard/mtk_ir/ir_common.h)

Third-party license notice is available in `THIRD_PARTY_NOTICES.md` and in the app under Settings → IR licenses.

## Build and checks

Open in Android Studio or build with Gradle. GitHub Actions checks all command maps and IR timing arrays, rejects Internet permission/ad dependencies, then assembles a debug APK. A phone must have a working Consumer IR emitter and support 38 kHz.
