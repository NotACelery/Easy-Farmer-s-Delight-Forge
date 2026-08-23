package dev.celerbi.easyfarmersdelightcompat.integration;

/**
 * Viewer-neutral semantic role for a tool in a documented Farmer harvest.
 *
 * <p>This is deliberately separate from {@link ToolRequirement}: ToolRequirement
 * describes a live machine blocker, while ToolUse documents whether a tool is
 * optional or mandatory for a recipe-viewer entry.</p>
 */
public enum ToolUse {
    NONE,
    OPTIONAL,
    REQUIRED
}
