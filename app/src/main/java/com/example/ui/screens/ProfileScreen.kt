package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.UserStatsEntity
import com.example.data.repository.FrenchLearningRepository
import com.example.ui.AuthViewModel
import com.example.ui.components.CefrLevelBadge
import com.example.ui.theme.BleuRoy
import com.example.ui.theme.OrAccent
import com.example.ui.theme.OrParis
import com.example.ui.theme.RougeFrance
import com.example.ui.theme.TextSecondaire
import com.example.ui.theme.VertSucces
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel,
    repository: FrenchLearningRepository,
    userStats: UserStatsEntity?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val currentUser by authViewModel.currentUser.collectAsState()
    val isLoading by authViewModel.isLoading.collectAsState()
    val errorMessage by authViewModel.errorMessage.collectAsState()

    val stats = userStats ?: UserStatsEntity()
    var isSyncing by remember { mutableStateOf(false) }
    var syncSuccessMsg by remember { mutableStateOf<String?>(null) }

    val cefrLevels = listOf(
        "A1" to "Débutant Découverte",
        "A2" to "Élémentaire Survie",
        "B1" to "Intermédiaire Seuil",
        "B2" to "Avancé Indépendant",
        "C1" to "Autonome / Fluence"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profil & Synchronisation", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // User Account Card
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth().testTag("profile_account_card")
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = if (currentUser != null) BleuRoy else Color(0xFFE2E8F0),
                                modifier = Modifier.size(54.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = if (currentUser != null) Color.White else TextSecondaire,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = currentUser?.displayName ?: stats.displayName,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = currentUser?.email ?: "Mode local (hors-ligne)",
                                    fontSize = 12.sp,
                                    color = TextSecondaire
                                )
                            }

                            CefrLevelBadge(level = stats.targetLevel)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Google Sign-In or Sign-Out Button
                        if (currentUser == null) {
                            Button(
                                onClick = { authViewModel.signInWithGoogle(context) },
                                enabled = !isLoading,
                                colors = ButtonDefaults.buttonColors(containerColor = BleuRoy),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("google_sign_in_btn")
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Connexion Google...")
                                } else {
                                    Icon(Icons.Default.AccountCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Se connecter avec Google")
                                }
                            }
                            Text(
                                text = "Sauvegardez vos progrès dans le Cloud pour les retrouver sur tous vos appareils.",
                                fontSize = 11.sp,
                                color = TextSecondaire,
                                modifier = Modifier.padding(top = 6.dp)
                            )
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = {
                                        isSyncing = true
                                        syncSuccessMsg = null
                                        coroutineScope.launch {
                                            try {
                                                repository.performFullCloudSync()
                                                syncSuccessMsg = "Données synchronisées avec succès !"
                                            } finally {
                                                isSyncing = false
                                            }
                                        }
                                    },
                                    enabled = !isSyncing,
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEFF6FF)),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f).testTag("sync_now_btn")
                                ) {
                                    if (isSyncing) {
                                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = BleuRoy, strokeWidth = 2.dp)
                                    } else {
                                        Icon(Icons.Default.CloudSync, contentDescription = null, tint = BleuRoy, modifier = Modifier.size(16.dp))
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Synchroniser", color = BleuRoy, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = { authViewModel.signOut(context) },
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.testTag("sign_out_btn")
                                ) {
                                    Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Déconnexion", fontSize = 12.sp)
                                }
                            }
                        }

                        if (!errorMessage.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = errorMessage!!,
                                fontSize = 12.sp,
                                color = RougeFrance
                            )
                        }

                        if (!syncSuccessMsg.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = syncSuccessMsg!!,
                                fontSize = 12.sp,
                                color = VertSucces,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // Target CEFR Level Selector
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Niveau Cible CECRL",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Ajustez la complexité des jeux de rôle et des examens proposés.",
                            fontSize = 12.sp,
                            color = TextSecondaire,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        cefrLevels.forEach { (lvl, desc) ->
                            val isSelected = stats.targetLevel == lvl
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) BleuRoy.copy(alpha = 0.1f) else Color.Transparent,
                                border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, BleuRoy) else null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable {
                                        // Save level to repository
                                    }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                                ) {
                                    CefrLevelBadge(level = lvl)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = desc,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) BleuRoy else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (isSelected) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = BleuRoy)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Offline & Cloud Cache Information
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Stockage Hors-Ligne & Données",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Toutes vos cartes mémoire, vos résultats d'examens et vos scores sont stockés localement sur cet appareil via Room Database pour un accès complet sans connexion internet. Lorsque vous êtes connecté, ils sont synchronisés en temps réel avec Firebase Firestore.",
                            fontSize = 12.sp,
                            color = TextSecondaire,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}
