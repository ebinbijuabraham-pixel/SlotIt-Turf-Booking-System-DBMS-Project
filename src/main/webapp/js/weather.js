/**
 * Weather API integration module for the venue detail page.
 */

// NOTE: Get a free API key from https://openweathermap.org/
const WEATHER_API_KEY = 'demo_key_replace_me';

/**
 * Fetch weather forecast for a specific location and date.
 * @param {number} lat - Latitude
 * @param {number} lng - Longitude
 * @param {Date} targetDate - The date to get the forecast for
 * @returns {Promise<Object>} Weather data object
 */
async function fetchWeather(lat, lng, targetDate) {
    if (WEATHER_API_KEY === 'demo_key_replace_me') {
        console.warn("Using demo API key. Returning fallback data.");
        return getFallbackWeather();
    }

    try {
        const response = await fetch(`https://api.openweathermap.org/data/2.5/forecast?lat=${lat}&lon=${lng}&appid=${WEATHER_API_KEY}&units=metric`);
        if (!response.ok) {
            throw new Error(`Weather API error: ${response.status}`);
        }
        
        const data = await response.json();
        const targetTime = targetDate.getTime();
        
        // Find the closest forecast to the target date
        let closestForecast = data.list[0];
        let minDiff = Math.abs(targetTime - new Date(closestForecast.dt_txt).getTime());

        for (let i = 1; i < data.list.length; i++) {
            const forecastTime = new Date(data.list[i].dt_txt).getTime();
            const diff = Math.abs(targetTime - forecastTime);
            if (diff < minDiff) {
                minDiff = diff;
                closestForecast = data.list[i];
            }
        }

        const condition = closestForecast.weather[0].main;
        return {
            temp: Math.round(closestForecast.main.temp),
            condition: condition,
            description: closestForecast.weather[0].description,
            icon: closestForecast.weather[0].icon,
            isRainy: ['Rain', 'Drizzle', 'Thunderstorm'].includes(condition),
            isSunny: condition === 'Clear'
        };
    } catch (error) {
        console.error("Failed to fetch weather data:", error);
        return getFallbackWeather();
    }
}

function getFallbackWeather() {
    return {
        temp: 28,
        condition: 'Clear',
        description: 'clear sky',
        icon: '01d',
        isRainy: false,
        isSunny: true
    };
}

/**
 * Render the weather card HTML based on weather data and venue type.
 * @param {Object} weatherData - The weather data object
 * @param {boolean} isOutdoor - Whether the venue is outdoor
 * @returns {string} HTML string for the weather card
 */
function renderWeatherCard(weatherData, isOutdoor) {
    let message = '';
    let cardClass = '';

    if (!isOutdoor) {
        cardClass = 'weather-indoor';
        message = `🏠 Indoor venue — weather won't affect your game`;
    } else if (weatherData.isRainy) {
        cardClass = 'weather-rainy';
        message = `⚠️ Rain expected at ${weatherData.temp}°C. Consider booking an indoor venue.`;
    } else if (weatherData.isSunny) {
        cardClass = 'weather-sunny';
        message = `☀️ Perfect weather for outdoor play! ${weatherData.temp}°C`;
    } else {
        cardClass = '';
        message = `⛅ Current weather: ${weatherData.condition}, ${weatherData.temp}°C`;
    }

    return `
        <div class="weather-card ${cardClass}">
            <div style="font-size: 2rem; line-height: 1;">${getWeatherEmoji(weatherData.condition)}</div>
            <div>
                <p class="fw-600">${message}</p>
                <p class="text-sm text-muted">${weatherData.description} ${getWeatherEmoji(weatherData.condition)}</p>
            </div>
        </div>
    `;
}

/**
 * Map weather condition to emoji.
 * @param {string} condition - Weather condition main text
 * @returns {string} Emoji string
 */
function getWeatherEmoji(condition) {
    switch (condition) {
        case 'Clear': return '☀️';
        case 'Clouds': return '⛅';
        case 'Rain': return '🌧️';
        case 'Drizzle': return '🌦️';
        case 'Thunderstorm': return '⛈️';
        case 'Snow': return '❄️';
        default: return '🌤️';
    }
}
