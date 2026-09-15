# HiVER / Konka RID 5040 Remote

ريموت Android أصلي يرسل إشارات الأشعة تحت الحمراء إلى تلفزيونات HiVER المتوافقة مع ملف Konka RID 5040.

## البناء

يفتح المشروع في Android Studio أو يُبنى عبر Gradle. يتطلب الهاتف مرسل IR فعلياً؛ التطبيق لا يعتمد على الإنترنت.

## ملاحظات التوافق

يتضمن التطبيق ملفي Aiwa/Konka عامين موثقين (KK-Y199 وKK-Y250A) مع اختيار مباشر وفحص «جوكر» لزر التشغيل. لا يرسل الفحص إلا أمراً حقيقياً من أحد الملفين، ولا توجد أزرار بتعيينات تخمينية. التلفزيون المستهدف هو HiVER H43F01 الذي يستجيب لملف Konka RID 5040 في ريموت TECNO.

يمكن الضغط مطولاً على أزرار الصوت والقنوات للإرسال المتكرر كما في الريموت الحقيقي.

الإصدار 1.3 يضيف لوحة اتجاهات فوق/تحت/يمين/يسار وOK. ملفا Aiwa المستخدمان مع HiVER يبقيان كما هما، وتظهر الاتجاهات فيهما معطلة لأن ملفات IRDB لا تحتوي هذه الأوامر. أُضيف ملف Konka STAOS اختياري مستند إلى تعريفات MStar/MediaTek العامة، ويستخدم بروتوكول Konka وتعمل معه الاتجاهات وOK؛ يجب اختباره من «ريموت جوكر» قبل حفظه لأن توافقه يختلف عن RID 5040.

## مصدر أكواد IR

- [IRDB — Konka KK-Y250A](https://github.com/probonopd/irdb/blob/master/codes/Konka/Unknown_KK-Y250A/25%2C1.csv)
- [IRDB — Konka KK-Y199](https://github.com/probonopd/irdb/blob/master/codes/Konka/Unknown_KK-Y199/25%2C1.csv)
- [تعريف Konka STAOS العام — أكواد الاتجاهات والبروتوكول](https://github.com/TCLOpenSource/mt9653/blob/master/drivers/input/keyboard/mtk_ir/keymaps/keymap-konka-tv.c)

كلا الملفين يستخدم بروتوكول Aiwa، الجهاز 25 والجهاز الفرعي 1. لا يحتاج التطبيق إذن إنترنت ولا يجمع بيانات.
