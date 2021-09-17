package com.kickstarter.services.transformers

import com.google.android.gms.common.util.Base64Utils
import com.kickstarter.models.ProjectFaq
import com.kickstarter.models.Relay
import java.nio.charset.Charset
import kotlin.math.absoluteValue

fun decodeRelayId(encodedRelayId: String?): Long? {
    return try {
        String(Base64Utils.decode(encodedRelayId), Charset.defaultCharset())
            .replaceBeforeLast("-", "", "")
            .toLong()
            .absoluteValue
    } catch (e: Exception) {
        null
    }
}

fun <T : Relay> encodeRelayId(relay: T): String {
    val classSimpleName = relay.javaClass.simpleName.replaceFirst("AutoParcel_", "")
    val id = relay.id()
    return Base64Utils.encodeUrlSafe(("$classSimpleName-$id").toByteArray(Charset.defaultCharset()))
}

fun projectFaqTransformer(faq: fragment.Faq?): ProjectFaq {
    val id = decodeRelayId(faq?.id() ?: "") ?: -1
    val answer = faq?.answer() ?: ""
    val createdAt = faq?.createdAt()
    val question = faq?.question() ?: ""

    return ProjectFaq.builder()
        .id(id)
        .answer(answer)
        .createdAt(createdAt)
        .question(question)
        .build()
}