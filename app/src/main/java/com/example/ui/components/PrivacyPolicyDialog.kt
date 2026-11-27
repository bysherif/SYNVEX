package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.ui.theme.AcidLime
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.IceWhite
import com.example.ui.theme.MistLavender
import com.example.ui.theme.NeonBorder
import com.example.ui.theme.NeonPanel
import com.example.ui.theme.NeonPanelElevated
import com.example.ui.theme.ObsidianVoid
import com.example.ui.theme.SolarGold

/**
 * In-app Privacy Policy dialog required by Google Play User Data Policy.
 * Displays clear Turkish + English data disclosures and links directly to Google's Privacy Policy
 * and the developer contact email.
 */
@Composable
fun PrivacyPolicyDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val privacyUrl = stringResource(R.string.privacy_policy_url)
    val developerEmail = stringResource(R.string.developer_contact_email)

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(Brush.verticalGradient(listOf(NeonPanelElevated, ObsidianVoid)))
                .border(1.8.dp, ElectricCyan.copy(alpha = 0.8f), RoundedCornerShape(22.dp))
                .padding(18.dp)
                .testTag("privacy_policy_dialog"),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.PrivacyTip,
                    contentDescription = null,
                    tint = ElectricCyan,
                    modifier = Modifier.size(24.dp)
                )
                Column {
                    Text(
                        text = stringResource(R.string.privacy_policy_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = IceWhite
                    )
                    Text(
                        text = "SYNVEX • Son Güncelleme: 26.11.2026",
                        style = MaterialTheme.typography.labelSmall,
                        color = ElectricCyan
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 320.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(NeonPanel.copy(alpha = 0.92f))
                    .border(1.dp, NeonBorder, RoundedCornerShape(14.dp))
                    .verticalScroll(rememberScrollState())
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                PrivacySectionBlock(
                    heading = "1. Yerel Oyun Verileri",
                    body = "Oyun ilerlemeniz (bölümler, haritalar, yükseltmeler, gemi tasarımları, Coin/Kristal bakiyesi ve günlük ödül süreleri) yalnızca kendi cihazınızda yerel olarak (Android DataStore) saklanır. Hesap açmanız veya kişisel bilgi paylaşmanız gerekmez."
                )
                PrivacySectionBlock(
                    heading = "2. Google Play Ödemeleri ve VIP Abonelikler",
                    body = "Tüm uygulama içi satın alımlar ve otomatik yenilenen VIP abonelikler (SYNVEX VIP PASS / ELİT PASS) doğrudan Google LLC (Google Play Billing) tarafından işlenir. SYNVEX kart veya ödeme bilgilerinizi görmez ve saklamaz. Aboneliğinizi dilediğiniz zaman Google Play Store → Abonelikler menüsünden iptal edebilirsiniz."
                )
                PrivacySectionBlock(
                    heading = "3. Toplamadığımız Veriler",
                    body = "Ad, e-posta, telefon, konum (GPS), kamera, mikrofon, rehber veya fotoğraf galerisi verisi toplamayız. Uygulama içinde üçüncü taraf reklam ağı veya reklam takip kimliği (AD_ID) kullanılmaz."
                )
                PrivacySectionBlock(
                    heading = "4. Veri Silme ve İletişim",
                    body = "Cihazınızdaki tüm oyun verilerini Android Ayarları → Uygulamalar → SYNVEX → Verileri Temizle veya uygulamayı kaldırarak kalıcı biçimde silebilirsiniz. Sorularınız için: $developerEmail"
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val targetWebUrl = if (privacyUrl.startsWith("https://")) {
                    privacyUrl
                } else {
                    "https://policies.google.com/privacy"
                }
                val webButtonLabel = if (privacyUrl.startsWith("https://")) {
                    "Web Sayfası"
                } else {
                    "Google Gizlilik"
                }
                OutlinedButton(
                    onClick = { openExternalUrl(context, targetWebUrl) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("privacy_open_web_button")
                ) {
                    Icon(Icons.Filled.OpenInNew, contentDescription = null, tint = AcidLime, modifier = Modifier.size(15.dp))
                    Spacer(Modifier.width(5.dp))
                    Text(webButtonLabel, style = MaterialTheme.typography.labelSmall, color = AcidLime, maxLines = 1)
                }
                OutlinedButton(
                    onClick = { openExternalUrl(context, "mailto:$developerEmail?subject=SYNVEX%20Gizlilik%20Politikasi") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("privacy_email_button")
                ) {
                    Icon(Icons.Filled.Email, contentDescription = null, tint = SolarGold, modifier = Modifier.size(15.dp))
                    Spacer(Modifier.width(5.dp))
                    Text("İletişim", style = MaterialTheme.typography.labelSmall, color = SolarGold, maxLines = 1)
                }
            }

            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElectricCyan,
                    contentColor = ObsidianVoid
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("privacy_close_button")
            ) {
                Text(
                    text = "ANLADIM / KAPAT",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
private fun PrivacySectionBlock(
    heading: String,
    body: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(
            text = heading,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = SolarGold
        )
        Text(
            text = body,
            style = MaterialTheme.typography.bodySmall,
            color = MistLavender
        )
    }
}

private fun openExternalUrl(context: Context, url: String) {
    runCatching {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}
