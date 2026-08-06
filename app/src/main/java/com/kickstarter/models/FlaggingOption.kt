package com.kickstarter.models

/**
 * A single node from the `flaggingOptions` GraphQL query, flattened into a list.
 *
 * The backend returns the flagging options tree as a flat, pre-order list. Each node carries its own
 * [id] (its path, e.g. `project/our_rules/prohibited_items`) and its
 * [parentId] (the parent's path).
 * [isGroup] nodes are expandable headings;
 * [kind] which is the value sent to `createFlagging`.
 */
data class FlaggingOption(
    val id: String,
    val parentId: String?,
    val kind: String?,
    val isGroup: Boolean,
    val title: String,
    val subtitle: String?,
    val placeholder: String?
)

/**
 * The root nodes of the flat list: those whose [FlaggingOption.parentId] is not itself the [id] of any
 * node in the list (the content-type root, e.g. `project`, is never returned as a node). Content-type
 * agnostic, so it avoids hardcoding the `"project"` sentinel.
 */
fun List<FlaggingOption>.roots(): List<FlaggingOption> {
    val ids = this.map { it.id }.toSet()
    return this.filter { it.parentId == null || it.parentId !in ids }
}

/**
 * The direct children of the node with the given [parentId], preserving the list's original order.
 */
fun List<FlaggingOption>.childrenOf(parentId: String?): List<FlaggingOption> =
    this.filter { it.parentId == parentId }
