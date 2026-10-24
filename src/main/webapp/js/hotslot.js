/**
 * HotSlot dynamic pricing module.
 */

/**
 * Calculate the final price based on peak status.
 * @param {number} hourlyRate - Base hourly rate
 * @param {number} peakMultiplier - Multiplier for peak hours
 * @param {boolean} isPeak - Whether the slot is during peak hours
 * @returns {number} Final calculated price
 */
function calculateFinalPrice(hourlyRate, peakMultiplier, isPeak) {
    let finalPrice = isPeak ? hourlyRate * peakMultiplier : hourlyRate;
    return parseFloat(finalPrice.toFixed(2));
}

/**
 * Get the HTML badge for the slot.
 * @param {boolean} isPeak - Whether the slot is peak
 * @param {string} status - Slot status ('available', 'booked', etc.)
 * @returns {string} HTML badge string
 */
function getSlotBadge(isPeak, status) {
    if (status === 'booked') {
        return '<span class="badge badge-error">Booked</span>';
    }
    if (isPeak) {
        return '<span class="badge badge-accent">🔥 Peak</span>';
    }
    return '<span class="badge badge-success">💰 Off-Peak Deal</span>';
}

/**
 * Render complete HTML for a slot card.
 * @param {Object} slot - Slot object containing details
 * @returns {string} HTML string for the slot card
 */
function renderSlotCard(slot) {
    const finalPrice = calculateFinalPrice(slot.hourlyRate, slot.peakMultiplier, slot.isPeak);
    const badgeHtml = getSlotBadge(slot.isPeak, slot.status);
    const timeDisplay = `${formatTime(slot.startTime)} – ${formatTime(slot.endTime)}`;
    
    const isAvailable = slot.status !== 'booked';
    
    let stateClass = '';
    let clickableAttr = isAvailable ? `data-slot-id="${slot.slotId}" onclick="selectSlot(${slot.slotId})"` : '';

    if (isAvailable) {
        if (slot.isPeak) {
            stateClass = 'peak';
        } else {
            stateClass = 'available';
        }
    } else {
        stateClass = 'booked';
    }

    return `
        <div class="slot-card ${stateClass}" ${clickableAttr}>
            <div class="flex justify-between items-center mb-8">
                <div>
                    <h4 class="fw-700">${timeDisplay}</h4>
                    <p class="text-xs text-muted mt-8">${slot.courtName}</p>
                </div>
                <div>${badgeHtml}</div>
            </div>
            <div class="flex justify-between items-center mt-16">
                <div class="fw-700">₹${finalPrice.toFixed(2)}</div>
                ${isAvailable ? '<button class="btn btn-sm btn-primary">Select</button>' : ''}
            </div>
        </div>
    `;
}

/**
 * Format time string from "HH:MM:SS" to "H:MM A"
 * @param {string} timeString - The time string to format
 * @returns {string} Formatted time string
 */
function formatTime(timeString) {
    if (!timeString) return '';
    const [hourStr, minuteStr] = timeString.split(':');
    let hour = parseInt(hourStr, 10);
    const ampm = hour >= 12 ? 'PM' : 'AM';
    hour = hour % 12;
    hour = hour ? hour : 12; // the hour '0' should be '12'
    return `${hour}:${minuteStr} ${ampm}`;
}
