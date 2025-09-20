package com.robertolopezaguilera.gimnasio.admob

//@Composable
//fun rememberInterstitialAdManager(
//    adUnitId: String = "ca-app-pub-3940256099942544/1033173712" // ID de prueba
//): InterstitialAdManager {
//    val context = LocalContext.current
//    val adManager = remember { InterstitialAdManager(context) }
//
//    LaunchedEffect(adUnitId) {
//        adManager.loadInterstitial(adUnitId)
//    }
//
//    DisposableEffect(Unit) {
//        onDispose {
//            adManager.interstitialAd = null
//        }
//    }
//
//    return adManager
//}