package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.LogoGold
import com.example.ui.theme.OrangeLightPrimary
import com.example.ui.theme.OrangeLightSoft
import com.example.ui.theme.SkyBluePrimary
import com.example.ui.theme.SkyBlueSoft
import com.example.ui.viewmodel.MainViewModel

@Composable
fun LicenseActivationDialog(
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    var inputKey by remember { mutableStateOf("") }
    var studentNameInput by remember { mutableStateOf("") }
    var studentContactInput by remember { mutableStateOf("") }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var isSuccess by remember { mutableStateOf(false) }
    val deviceId = remember { viewModel.securityManager.getOrCreateDeviceId() }
    val clipboardManager = LocalClipboardManager.current

    Dialog(onDismissRequest = onDismiss) {
        Mind3DCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            tone = Mind3DCardTone.Sky,
            shapeRadius = 24.dp,
            extrusionDepth = 5.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .imePadding()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isSuccess) Color(0xFFE8F5E9) else SkyBlueSoft)
                        .padding(14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isSuccess) Icons.Default.CheckCircle else Icons.Default.LockOpen,
                        contentDescription = null,
                        tint = if (isSuccess) Color(0xFF2E7D32) else SkyBluePrimary
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = if (isSuccess) "دوره با موفقیت فعال شد" else "دروازه یکپارچه فعال‌سازی دوره",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "تنها با ثبت یک کلید معتبر امضاشده از مدرس (اکبر شیرزادخانی)، تمام ۸ کتاب، ۸ فصل، ۱۲ بازی و ابزارهای تمرین بدون هیچ قفل جداگانه‌ای باز خواهند شد.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(OrangeLightSoft.copy(alpha = 0.5f))
                        .border(1.dp, OrangeLightPrimary.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .padding(10.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "شناسه اختصاصی دستگاه شما:",
                            style = MaterialTheme.typography.labelSmall,
                            color = OrangeLightPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = deviceId,
                            style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 1.sp),
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFBF360C)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Mind3DChip(
                            text = "کپی شناسه دستگاه",
                            onClick = {
                                clipboardManager.setText(AnnotatedString(deviceId))
                                statusMessage = "شناسه دستگاه کپی شد."
                            },
                            icon = Icons.Default.ContentCopy,
                            tone = Mind3DChipTone.Peach,
                            isSelected = true
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (!isSuccess) {
                    OutlinedTextField(
                        value = studentNameInput,
                        onValueChange = { studentNameInput = it },
                        label = { Text("نام و نام خانوادگی شما") },
                        placeholder = { Text("مثال: مریم محمدی") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("license_student_name_input"),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SkyBluePrimary,
                            unfocusedBorderColor = Color(0xFFCBD5E1)
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = studentContactInput,
                        onValueChange = { studentContactInput = it },
                        label = { Text("شماره تماس یا ایمیل (اختیاری)") },
                        placeholder = { Text("09XX... یا email@example.com") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SkyBluePrimary,
                            unfocusedBorderColor = Color(0xFFCBD5E1)
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = inputKey,
                        onValueChange = { inputKey = it },
                        label = { Text("کلید فعال‌سازی امضاشده") },
                        placeholder = { Text("MIND-XXXXXXXX-XXXX") },
                        leadingIcon = {
                            Icon(Icons.Default.Key, contentDescription = null, tint = LogoGold)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("license_key_input"),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SkyBluePrimary,
                            unfocusedBorderColor = Color(0xFFCBD5E1)
                        )
                    )

                    if (statusMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = statusMessage!!,
                            color = if (statusMessage!!.contains("کپی شد")) Color(0xFF0369A1) else Color(0xFFD32F2F),
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Mind3DButton(
                        text = "بررسی و فعال‌سازی کل دوره",
                        onClick = {
                            viewModel.activateLicenseKey(inputKey) { success, msg ->
                                isSuccess = success
                                statusMessage = msg
                            }
                        },
                        variant = Mind3DButtonVariant.Sky,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("activate_license_button")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "کلید فعال‌سازی ندارید؟ درخواست خود را به مدرس ارسال کنید:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Mind3DButton(
                            text = "پیامک به مدرس",
                            onClick = {
                                val name = studentNameInput.ifBlank { "هنرجو" }
                                viewModel.requestKeySms(name, studentContactInput)
                            },
                            variant = Mind3DButtonVariant.Orange,
                            icon = Icons.Default.Sms,
                            modifier = Modifier.weight(1f),
                            height = 48.dp
                        )

                        Mind3DButton(
                            text = "ایمیل به مدرس",
                            onClick = {
                                val name = studentNameInput.ifBlank { "هنرجو" }
                                viewModel.requestKeyEmail(name, studentContactInput)
                            },
                            variant = Mind3DButtonVariant.Emerald,
                            icon = Icons.Default.Email,
                            modifier = Modifier.weight(1f),
                            height = 48.dp
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "تمام ۸ کتاب، ۸ فصل، ۱۲ بازی شناختی، آزمون‌ها و فلش‌کارت‌ها به صورت کامل روی این دستگاه فعال شدند.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF2E7D32),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Mind3DButton(
                        text = "ورود به محتوای دوره",
                        onClick = onDismiss,
                        variant = Mind3DButtonVariant.Emerald,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Mind3DButton(
                    text = "بستن",
                    onClick = onDismiss,
                    variant = Mind3DButtonVariant.Neutral,
                    modifier = Modifier.fillMaxWidth(),
                    height = 48.dp
                )
            }
        }
    }
}
