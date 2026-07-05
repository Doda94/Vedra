# Design prompt — Vedra weather app

> Copy everything below this line into Claude Design (or any design tool/designer).

---

Design a complete, production-ready **Material 3 Expressive** design for **Vedra**, a native Android weather app for Croatia. Vedra means "serene/clear (sky)" in Croatian. All data comes from DHMZ — the Croatian Meteorological and Hydrological Service (meteo.hr). The app is Croatian-first with an English localization. Deliver every screen in **both light and dark mode**.

## 1. Product idea

A calm, trustworthy, beautifully Croatian weather app. It is NOT a global weather app: it covers Croatia deeply (324 forecast locations, official alerts, the Adriatic sea, wildfire danger, UV, biometeorology) plus a simple "Europe today" table. Its personality: official data, expressive presentation. Think "the DHMZ data you trust, designed like a first-party Google app."

## 2. Brand

- Name: **Vedra**
- Seed colors (already tuned into full M3 light/dark schemes):
  - Primary `#2196F3` — sky / clear weather
  - Secondary `#607D8B` — cloud / overcast
  - Tertiary `#DB7900` — sun / warm accent
  - Neutral `#73777E` — surfaces / dividers
- Typography: **Manrope** for display/headline/title, **Inter** for body/label (both already bundled).
- Dynamic color (Material You) exists as an optional setting, but the brand palette is the default — design with the brand palette.

## 3. Material 3 Expressive requirements

Use the expressive component set and motion, not classic M3:

- `MaterialExpressiveTheme` with expressive motion scheme (springy, bouncy transitions).
- Large flexible top app bars that collapse on scroll.
- Grouped lists with the expressive mixed-corner treatment (first row large top radii, middle rows small radii, last row large bottom radii).
- Pill-shaped chips, segmented button groups for day/tab switching.
- Shape morphing on press where it makes sense (e.g. favorite star, refresh).
- The expressive wavy/squiggle progress and loading indicators.
- Oversized display typography for the hero temperature.
- Tonal containers (primary/secondary/tertiary containers) instead of elevation-heavy cards.
- Edge-to-edge, bottom navigation bar with 3 destinations.

## 4. Exactly what data we have (design only around this)

All data refreshes roughly **hourly** from DHMZ XML files. Times are Croatian local time (Europe/Zagreb). There is NO minute-by-minute nowcast, NO radar, NO satellite imagery, NO pollen counts, NO air quality index. Don't design features that need data we don't have.

### 4.1 Current observations (~40 stations across Croatia)
Per station: name, coordinates, temperature °C, relative humidity %, air pressure hPa + 3-hour tendency (rising/falling), wind direction (N/NE/E/SE/S/SW/W/NW or calm) + speed m/s, weather description text in Croatian ("vedro", "oblačno", "kiša"...), weather symbol code. Snapshot has a date + hour of measurement (e.g. "today at 14 h").
**Derived by the app:** feels-like temperature (apparent temp, heat index, wind chill).

### 4.2 Seven-day forecast (324 Croatian cities and places)
Per city, hourly slots for 7 days: temperature °C, DHMZ weather symbol code (numeric, with day/night variants — e.g. "2" and "2n"), wind token (direction + strength class 1–3, e.g. "SW1", "N3", or "C" for calm), precipitation amount mm, precipitation probability %.
**Derived by the app, per day:** min/max temp, total precipitation, max precipitation probability, dominant weather symbol.
This is the richest dataset — it powers the hourly chart and the 7-day list.

### 4.3 National daily forecast (today + tomorrow)
Per macro-region (Central Croatia, Eastern Croatia, Mountainous Croatia, Istria, Dalmatia, Northern/Central/Southern Adriatic...): min/max temp, symbol, wind code, warning level. Plus two narrative texts written by DHMZ meteorologists: one for all of Croatia, one specifically for Zagreb. These narratives are good editorial content — give them room.

### 4.4 Multi-day outlook
A narrative summary text plus per-day regional entries (land/sea) for the days beyond tomorrow.

### 4.5 Regional descriptions
Free-form forecast text per region (Eastern, Central, Northern Adriatic, Mountainous, Dalmatia, Istria).

### 4.6 Official weather alerts (CAP / Meteoalarm) — today, tomorrow, day after tomorrow
Per alert: event name, severity (**green / yellow / orange / red**), hazard type (wind, snow/ice, thunderstorm, fog, high temp, low temp, coastal event, forest fire, avalanche, rain, flood), onset and expiry timestamps, full description + safety instructions in **both Croatian and English**, affected area names and region codes (mappable to cities).
Severity must be readable by color-blind users: icon + label + color, never color alone.

### 4.7 Hydrological bulletin
Forecast period, a warning text, and status text per river: Sava, Kupa, Danube, Mura, Drava, Neretva.

### 4.8 Marine / Adriatic
- General marine forecast: title, optional warning, sea state, forecast text for first 12 h and next 12 h.
- Sailors' forecast: per Adriatic region (multiple named zones), forecast texts + valid-until.
- Sea temperature: ~15 coastal stations × several measurement hours per day (e.g. 8 h, 11 h, 14 h).

### 4.9 Indices
- **UV index**: per station, values per daytime hour, each with an official color category.
- **Biometeorological forecast**: narrative text per day + a numeric comfort level per region (multi-day).
- **Fire danger** (Adriatic fire season): per station, danger category (very low → very high) + FWI technical indices.
- **Heat wave / cold wave**: per city, warning level for the next 5 days (mappable — cities have coordinates).

### 4.10 Europe today
Current weather for ~30 European capitals/cities: temperature, humidity, pressure, wind, weather text + symbol.

### 4.11 Yesterday's climate extremes
Per station: minimum temperature, maximum temperature, 5 cm ground temperature, 24-h precipitation.

## 5. Screens to design

1. **Home / Danas** — selected city hero (huge temp, symbol, feels-like, wind, humidity), active alert banner if any, hourly strip/chart, 7-day grouped list, DHMZ narrative for Croatia/Zagreb, last-updated footer, pull-to-refresh.
2. **City picker** — search over 324 places, favorites pinned with star, recent, "use my location" (finds nearest place).
3. **City detail** — full hourly temperature chart (7 days, scrubbing), precipitation amount + probability, wind per slot, daily breakdown.
4. **Alerts / Upozorenja** — segmented control for today/tomorrow/day-after, severity-tinted cards, region chips; tapping opens a bottom sheet with full description + instructions and an HR/EN language toggle; empty state ("no alerts — vedro!") matters.
5. **Marine / Jadran** — sea state + 12 h/next 12 h forecast, sailors' zones, sea temperature table by station and hour.
6. **Indices** — UV (value + category color per hour), bio forecast, fire danger map-or-list, heat/cold wave 5-day levels.
7. **Europe** — sortable simple table/grid of cities.
8. **Yesterday** — extremes (warmest/coldest place, most rain) presented as highlights, then full station list.
9. **Settings** — default city, dynamic color toggle, language (HR/EN), units display, about + mandatory "Data: DHMZ (meteo.hr)" attribution.

Navigation: bottom bar with 3 tabs — **Danas** (home), **Upozorenja** (alerts), **Istraži** (explore: marine, indices, Europe, yesterday). City picker and settings reached from the home top app bar.

## 6. States to design

For every data screen: loading (expressive loading indicator), error with retry, offline-with-cached-data ("Zadnje ažurirano prije 42 min" banner), and empty states. The alert-free empty state should feel positive, on-brand ("vedro").

## 7. Weather symbol system

DHMZ symbols are numeric codes with day/night variants. Design a custom Vedra icon set (or specify a mapping style) covering: clear, partly cloudy, cloudy, fog, rain, showers, thunderstorm, snow, sleet, plus night variants. Consistent stroke style, works at 16 dp (lists) up to 96 dp (hero).

## 8. Accessibility and localization

- WCAG AA contrast (4.5:1) in both modes, including text on severity colors.
- Severity/danger always icon + label + color.
- Croatian is the primary language (expect long words — "grmljavinsko nevrijeme"); English second. No RTL needed.
- Dynamic type: layouts must survive large font scaling.

## 9. Deliverables

- Full screen set (all 9 screens + key states), light and dark.
- Color token sheet (mapping to M3 roles), type scale, spacing/shape scale.
- Component specs: alert card, forecast row, hourly chip/chart, index tile, hero card, bottom sheet.
- Severity color ramp (green/yellow/orange/red) tuned for both modes with accessible on-colors.
- Motion notes: which transitions use the expressive spring scheme (tab change, city switch, alert sheet, pull-to-refresh).

Start with the Home screen and Alerts screen, since they set the visual language for everything else.
