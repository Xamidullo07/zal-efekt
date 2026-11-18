# Konsert Zali — tizim bo'ylab ovoz effekti (Android)

Mashina monitorida (Android) chalinayotgan **barcha ovozga** — YouTube, brauzer, musiqa pleyerlari —
konsert zali (reverb) effektini qo'shadi. Sozlamalar: zal kuchi, zal uzunligi, stereo kengligi, bas.

## APK olish (2 usuldan biri)

### 1-usul: GitHub orqali (kompyuterga hech narsa o'rnatmasdan)
1. github.com'da bepul hisob oching va yangi repozitoriy yarating.
2. Shu papkadagi **barcha fayllarni** (yashirin `.github` papkasi bilan) repozitoriyga yuklang.
3. Repozitoriyda **Actions** bo'limiga kiring → "APK yig'ish" ishi tugashini kuting (3–5 daqiqa).
4. Tugagan ishni oching → pastda **Artifacts** → `KonsertZali-apk` ni yuklab oling → ichidan `app-debug.apk`.

### 2-usul: Android Studio
1. Android Studio'da shu papkani oching (File → Open).
2. Build → Build APK(s). Fayl: `app/build/outputs/apk/debug/app-debug.apk`.

## O'rnatish
1. `app-debug.apk` ni USB flesh yoki telefon orqali mashina monitoriga o'tkazing.
2. Fayl menejeridan oching, "Noma'lum manbalardan o'rnatish"ga ruxsat bering.
3. Dasturni oching → **"Effekt yoqilgan"** ni yoqing → YouTube yoki boshqa dasturda musiqa qo'ying.

## Muhim cheklov
Dastur Android'ning tizim audio effektlaridan (session 0) foydalanadi. Ba'zi mashina monitorlari
(ayniqsa yangi Android 12+ yoki yopiq proshivkali) bu effektlarni qabul qilmaydi. Bunday holda dastur
ichidagi holat satrida "YO'Q" ko'rinadi va ovoz o'zgarmaydi — bu dastur xatosi emas, qurilma cheklovi.
Ba'zi monitorlarda ovoz alohida DSP/kuchaytirgich orqali o'tadi va Android effektlari unga yetib bormaydi.
