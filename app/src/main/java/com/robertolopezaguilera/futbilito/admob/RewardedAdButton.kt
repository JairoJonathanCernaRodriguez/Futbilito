package com.robertolopezaguilera.futbilito.admob

import android.app.Activity
import android.util.Log
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

// Estado observable para Compose
class RewardedAdState {
    var rewardedAd: RewardedAd? by mutableStateOf(null)
    var isLoading: Boolean by mutableStateOf(false)
    var loadError: String? by mutableStateOf(null)
}

@Composable
fun rememberRewardedAdState(): RewardedAdState {
    val context = LocalContext.current
    val state = remember { RewardedAdState() }

    LaunchedEffect(Unit) {
        loadRewardedAd(context, state)
    }

    return state
}

fun loadRewardedAd(context: android.content.Context, state: RewardedAdState) {
    state.isLoading = true
    state.loadError = null

    val adRequest = AdRequest.Builder().build()
    val adUnitId = "ca-app-pub-3940256099942544/5224354917"

    RewardedAd.load(
        context,
        adUnitId,
        adRequest,
        object : RewardedAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.e("RewardedAd", "Error: ${adError.message}")
                state.isLoading = false
                state.rewardedAd = null
                state.loadError = adError.message
            }

            override fun onAdLoaded(ad: RewardedAd) {
                Log.d("RewardedAd", "Anuncio cargado ✅")
                state.isLoading = false
                state.rewardedAd = ad
                state.loadError = null
            }
        }
    )
}

// Función para mostrar el anuncio y manejar la recompensa
fun showRewardedAd(
    activity: Activity,
    state: RewardedAdState,
    onRewardEarned: (RewardItem) -> Unit,
    onAdDismissed: () -> Unit = {}, // ← AÑADIR callback opcional
    onAdFailed: () -> Unit = {} // ← AÑADIR callback opcional
) {
    val ad = state.rewardedAd
    if (ad != null) {
        Log.d("RewardedAd", "Mostrando anuncio...")

        ad.fullScreenContentCallback = object : com.google.android.gms.ads.FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Log.d("RewardedAd", "Anuncio cerrado")
                onAdDismissed() // ← EJECUTAR callback
                loadRewardedAd(activity, state)
            }

            override fun onAdFailedToShowFullScreenContent(p0: com.google.android.gms.ads.AdError) {
                Log.e("RewardedAd", "Error mostrando anuncio: ${p0.message}")
                onAdFailed() // ← EJECUTAR callback
            }
        }

        ad.show(activity) { rewardItem ->
            Log.d("RewardedAd", "🎉 Recompensa recibida: ${rewardItem.amount} ${rewardItem.type}")
            onRewardEarned(rewardItem)
        }
    } else {
        Log.e("RewardedAd", "No hay anuncio para mostrar ❌")
        onAdFailed() // ← EJECUTAR callback si no hay anuncio
        loadRewardedAd(activity, state)
    }
}