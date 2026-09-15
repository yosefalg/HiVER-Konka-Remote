# HiVER / Konka RID 5040 Remote

ريموت Android أصلي يرسل إشارات الأشعة تحت الحمراء إلى تلفزيونات HiVER المتوافقة مع ملف Konka RID 5040.

## البناء

يفتح المشروع في Android Studio أو يُبنى عبر Gradle. يتطلب الهاتف مرسل IR فعلياً؛ التطبيق لا يعتمد على الإنترنت.

## ملاحظات التوافق

يتضمن التطبيق ملفي Aiwa/Konka عامين موثقين (KK-Y199 وKK-Y250A) مع اختيار مباشر وفحص «جوكر» لزر التشغيل. لا يرسل الفحص إلا أمراً حقيقياً من أحد الملفين، ولا توجد أزرار بتعيينات تخمينية. التلفزيون المستهدف هو HiVER H43F01 الذي يستجيب لملف Konka RID 5040 في ريموت TECNO.

## مصدر أكواد IR

- [IRDB — Konka KK-Y250A](https://github.com/probonopd/irdb/blob/master/codes/Konka/Unknown_KK-Y250A/25%2C1.csv)
- [IRDB — Konka KK-Y199](https://github.com/probonopd/irdb/blob/master/codes/Konka/Unknown_KK-Y199/25%2C1.csv)

كلا الملفين يستخدم بروتوكول Aiwa، الجهاز 25 والجهاز الفرعي 1. لا يحتاج التطبيق إذن إنترنت ولا يجمع بيانات.
