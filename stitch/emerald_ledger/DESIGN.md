# The Design System: Editorial Finance

## 1. Overview & Creative North Star: "The Financial Curator"
This design system rejects the cluttered, anxiety-inducing aesthetics of traditional banking. Our Creative North Star is **The Financial Curator**. We treat a user's wealth not as a series of rows in a database, but as a curated gallery of their life’s progress. 

To achieve this, we move beyond "standard" Fintech templates. We utilize **intentional asymmetry**, high-contrast typography scales, and a philosophy of **Tonal Layering**. By eschewing traditional borders in favor of soft depth and "glass" surfaces, we create an environment that feels premium, authoritative, and breathtakingly calm.

---

## 2. Colors & Surface Philosophy
The palette is anchored in `primary` (#001e40 - Deep Navy) for authority and `secondary` (#006c49 - Emerald) for growth. 

### The "No-Line" Rule
**Explicit Instruction:** Designers are prohibited from using 1px solid borders to section content. Boundaries must be defined solely through background color shifts.
*   **Method:** Place a `surface_container_lowest` card on top of a `surface_container_low` background. 
*   **Goal:** To eliminate "visual noise" and let the content breathe.

### Surface Hierarchy & Nesting
Treat the UI as a physical stack of fine paper.
*   **Base Layer:** `surface` (#f8f9fa)
*   **Sectioning:** `surface_container_low` for large structural areas.
*   **Interactive Cards:** `surface_container_lowest` (#ffffff) to provide "pop" and focus.
*   **Deep Contrast:** Use `primary_container` (#003366) for high-impact summary sections (e.g., Total Net Worth) to create a sophisticated "anchor" for the eye.

### The "Glass & Gradient" Rule
For floating action buttons or high-priority modals, utilize **Glassmorphism**:
*   **Fill:** `surface` at 70% opacity.
*   **Effect:** `backdrop-blur: 20px`.
*   **CTA Soul:** Main buttons should use a subtle linear gradient from `secondary` (#006c49) to `on_secondary_container` (#00714d) at a 135-degree angle. This adds "bioluminescent" depth to the Emerald green.

---

## 3. Typography: The Editorial Scale
We use a dual-sans serif approach to balance character with utility. **Manrope** provides a modern, geometric feel for high-level data, while **Inter** ensures legendary legibility for transactional details.

*   **Display (Manrope):** Use `display-lg` (3.5rem) for big numbers—account balances and "hero" metrics. It should feel like a headline in a premium financial magazine.
*   **Headlines (Manrope):** `headline-md` (1.75rem) for page titles, emphasizing the "Curator" voice.
*   **Body (Inter):** `body-lg` (1rem) for all descriptions. Inter’s tall x-height ensures clarity even in dense financial lists.
*   **Labels (Inter):** `label-md` (0.75rem) in `on_surface_variant` (#43474f) for metadata (dates, categories).

---

## 4. Elevation & Depth
In this system, "Elevation" is a feeling, not a drop-shadow preset.

*   **The Layering Principle:** Avoid shadows for static cards. Instead, use the `surface_container` tokens. An "active" state is represented by shifting from `surface_container_lowest` to `surface_bright`.
*   **Ambient Shadows:** If a card must "float" (e.g., a draggable transaction), use a shadow with `blur: 40px`, `y: 12px`, and color `primary` at **4% opacity**. This mimics natural light rather than a digital "glow."
*   **The "Ghost Border" Fallback:** For accessibility in dark mode or high-glare environments, use a `1px` stroke of `outline_variant` at **15% opacity**. It should be felt, not seen.

---

## 5. Components & Primitives

### Cards & Lists (The Core)
*   **Forbid Dividers:** Never use a horizontal line between transactions. Use `1.5rem` (`md`) vertical spacing.
*   **Layout:** Use an asymmetrical layout for cards—place the category icon in a large `secondary_fixed` (#6ffbbe) circle (Radius: `xl`) on the left, with the amount in `headline-sm` on the right.

### Buttons
*   **Primary:** Background `primary`, Text `on_primary`. Radius: `md` (1.5rem).
*   **Secondary (The "Emerald" State):** Background `secondary_container`, Text `on_secondary_container`. Use this for "positive" actions like "Add Funds."
*   **Ghost:** No background, `outline` text. Only for low-priority actions.

### Inputs
*   **Style:** Minimalist. No bottom line. Use `surface_container_high` as a filled background with `xl` (3rem) rounded corners.
*   **Active State:** Transition background to `surface_lowest` and add a `2px` "Ghost Border" of `primary` at 20% opacity.

### Contextual Components for Fintech
*   **The Progress Ring:** Use `secondary` for the "completed" portion and `secondary_container` for the "remaining" track.
*   **Micro-Trend Sparklines:** Simplified SVG paths with no axes, using `secondary` for growth and `error` for loss, placed inside transaction items.

---

## 6. Do’s and Don’ts

### Do:
*   **Embrace Negative Space:** If you think you need a divider, add 16px of padding instead.
*   **Use Large Radii:** Every card and button must use at least `DEFAULT` (1rem) or `md` (1.5rem) to maintain the "Friendly Fintech" persona.
*   **Tonal Consistency:** Ensure the `on_surface` text always has enough contrast against the specific `surface_container` tier being used.

### Don’t:
*   **Don't use Pure Black:** Even in dark mode, the darkest color should be `primary` (#001e40).
*   **Don't use 100% Opaque Borders:** This shatters the "Editorial" feel and makes the app look like a generic bootstrap site.
*   **Don't Over-Shadow:** If more than three elements on a screen have shadows, the hierarchy is broken. Use tonal shifts first.

---

## 7. Token Reference Summary

| Token | Value | Usage |
| :--- | :--- | :--- |
| `primary` | #001e40 | Main branding, Headers, Primary CTAs |
| `secondary` | #006c49 | Positive growth, Success states |
| `surface` | #f8f9fa | Main background canvas |
| `surface_container_lowest`| #ffffff | High-priority content cards |
| `radius-xl` | 3rem | Oversized "pill" buttons and container corners |
| `radius-lg` | 2rem | Standard financial data cards |