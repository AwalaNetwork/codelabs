package com.example.pingcodelab

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import tech.relaycorp.awaladroid.GatewayClient
import tech.relaycorp.awaladroid.messaging.OutgoingMessage
import tech.relaycorp.awaladroid.messaging.ParcelId
import java.time.ZonedDateTime
import java.util.*

@ExperimentalCoroutinesApi
class MainActivity : AppCompatActivity() {
    private val context by lazy { applicationContext as App }

    private val backgroundContext = lifecycleScope.coroutineContext + Dispatchers.IO
    private val backgroundScope = CoroutineScope(backgroundContext)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        lifecycleScope.launch {
            // Wait for the app setup to complete if it's still going
            context.setupDone.receive()

            send.isEnabled = true
        }

        context.pingRepository
            .observe()
            .onEach { pings.text = formatPings(it) }
            .launchIn(lifecycleScope)

        send.setOnClickListener {
            backgroundScope.launch {
                sendPing()
            }
        }

        clear.setOnClickListener {
            backgroundScope.launch {
                context.pingRepository.clear()
            }
        }
    }

    private fun formatPings(it: List<PingMessage>) =
        it.joinToString("\n---\n") { ping ->
            val pingDate = Date(ping.sent)
            val pongDate = ping.received?.let {
                Date(ping.received)
            } ?: "Pending"
            val shortId = ping.id.takeLast(6)
            listOf(
                "Ping $shortId:",
                "- Sent time: $pingDate",
                "- Pong reception time: $pongDate"
            ).joinToString("\n")
        }

    private suspend fun sendPing() {
        // TODO
    }

    override fun onDestroy() {
        super.onDestroy()

        // Unbind from the gateway if still bound
        GatewayClient.unbind()
    }
}
