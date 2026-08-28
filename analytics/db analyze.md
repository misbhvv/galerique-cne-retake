3## Tabel: Schaalniveaus (gebruikers → requests → data)

| Daily users | Requests/day | Tekstdata/maand (GB) | Afbeeldingen/maand (GB) |
| ----------- | ------------ | -------------------- | ----------------------- |
| 0           | 0            | 0.00000              | 0.00000                 |
| 5           | ~20          | 0.01000              | 0.20000                 |
| 50          | ~200         | 0.10000              | 2.00000                 |
| 500         | ~2 000       | 0.50000              | 10.00000                |
| 5 000       | ~20 000      | 2.00000              | 40.00000                |
| 50 000      | ~200 000     | 10.00000             | 200.00000               |
| 500 000     | ~2 000 000   | 50.00000             | 400.00000               |

_Deze kolom toont de globale hoeveelheid tekstuele metadata en object‑storage voor foto’s._

---

## SQL opties op Azure

**Belangrijkste categorieën:**

1. **SQL Managed (DTU)** – vaste prestatie‑laag (Basic / S0 / S1 / …) met een vaste prijs per maand.
2. **SQL Managed (vCore‑provisioned)** – je kiest aantal vCores en betaalt per uur/maand.
3. **SQL Managed (Serverless vCore)** – auto‑scale/auto‑pause voor onregelmatige workloads (kan goedkoper zijn bij lage traffic).

De tabellen hieronder schatten de **maandelijkse kosten bij verschillende gebruiksniveaus**. De kolom **Estimated Query Workload Impact** geeft de relatieve impact op performance/cost — een hogere waarde betekent zwaardere belasting/meer verwerking per request.

---

### Tabel A — Azure SQL Managed (Basic/DTU model)

**Beschrijving:** Relationale database met vaste DTU‑prestatie. Goed bij voorspelbare workloads; goedkoopste SQL‑optie voor kleine apps.

| Daily users | Estimated Query Workload Impact | Approx. monthly cost SQL |
| ----------- | ------------------------------- | ------------------------ |
| 0           | 1.00000                         | ~5.00000                 |
| 5           | 1.02000                         | ~5.00000                 |
| 50          | 1.05000                         | ~5.00000                 |
| 500         | 1.10                            | ~10.00000                |
| 5 000       | 1.20                            | ~20.00000                |
| 50 000      | 1.40                            | ~40.00000                |
| 500 000     | 1.80                            | ~70.00000                |

---

### Tabel B — Azure SQL Managed (vCore Provisioned)

**Beschrijving:** Relationale database met vooraf gekozen compute‑capacity. Sterker en voorspelbaar bij groei; kost meer zodra data/requests toenemen.

| Daily users | Estimated Query Workload Impact | Approx. monthly cost SQL |
| ----------- | ------------------------------- | ------------------------ |
| 0           | 1.00000                         | ~10.00000                |
| 5           | 1.01000                         | ~10.00000                |
| 50          | 1.04000                         | ~12.00000                |
| 500         | 1.12                            | ~25.00000                |
| 5 000       | 1.30                            | ~50.00000                |
| 50 000      | 1.60                            | ~100.00000               |
| 500 000     | 2.00                            | ~180.00000               |

---

### Tabel C — Azure SQL Serverless (vCore Auto‑Pause/Auto‑Scale)

**Beschrijving:** Betalingen op basis van vCore‑gebruik en auto‑pause bij inactiviteit. Voordelig als er veel idle tijd is.

| Daily users | Estimated Query Workload Impact | Approx. monthly cost SQL |
| ----------- | ------------------------------- | ------------------------ |
| 0           | 1.00000                         | ~1.00000                 |
| 5           | 1.01000                         | ~2.00000                 |
| 50          | 1.03000                         | ~5.00000                 |
| 500         | 1.15                            | ~15.00000                |
| 5 000       | 1.35                            | ~40.00000                |
| 50 000      | 1.70                            | ~80.00000                |
| 500 000     | 2.20                            | ~160.00000               |

_Kan veel goedkoper zijn als de DB veel “slaapstand” heeft; maar bij continue traffic gaat de factuur snel omhoog._

---

## NoSQL opties op Azure (Cosmos DB)

Azure Cosmos DB biedt drie belangrijke varianten:

- **Serverless** – pay‑per‑requests (RU’s) en opslag; geen minimum throughput.
- **Provisioned throughput (manual)** – je reserveert RU/s, vaste kosten.
- **Provisioned (autoscale)** – provisioned maar kan automatisch schalen met max RU/s. Deze zijn beschikbaar via de NoSQL API, MongoDB API, Table API, enz.

---

### Tabel D — Cosmos DB Serverless (NoSQL)

**Beschrijving:** Document store, pay‑per‑request billing (RU). Goed voor wisselende traffic en ontwikkel/test‑omgevingen.

| Daily users | Estimated Query Workload Impact | Approx. monthly cost Cosmos DB (serverless) |
| ----------- | ------------------------------- | ------------------------------------------- |
| 0           | 1.00000                         | ~1.25000 (min billable 5 GB storage)        |
| 5           | 1.02000                         | ~2.00000                                    |
| 50          | 1.05                            | ~5.00000                                    |
| 500         | 1.15                            | ~10.00000                                   |
| 5 000       | 1.30                            | ~25.00000                                   |
| 50 000      | 1.60                            | ~50.00000                                   |
| 500 000     | 2.10                            | ~120.00000                                  |

---

### Tabel E — Cosmos DB Provisioned (manual throughput)

**Beschrijving:** Je kiest RU/s (bv 400 RU/s start), vaste maandelijkse tarieven tot het einde van de maand, factoren zoals index size + storage komen erbij.

| Daily users | Estimated Query Workload Impact | Approx. monthly cost Cosmos (provisioned) |
| ----------- | ------------------------------- | ----------------------------------------- |
| 0           | 1.00000                         | ~20.00000                                 |
| 5           | 1.01                            | ~20.00000                                 |
| 50          | 1.04                            | ~22.00000                                 |
| 500         | 1.10                            | ~30.00000                                 |
| 5 000       | 1.25                            | ~50.00000                                 |
| 50 000      | 1.55                            | ~90.00000                                 |
| 500 000     | 2.00                            | ~180.00000                                |

_Waarbij 400 RU/s ≈ ~$20/maand aan minimale throughput is._

---

## Vergelijkingstabel: Gecombineerde kostenscore (Impact × Maandkost)

_Elke cel = Estimated Query Workload Impact × Approx. monthly cost voor die database bij dat gebruikersniveau._

| Daily users | Azure SQL Managed (Basic/DTU) | Azure SQL Managed (vCore Provisioned) | Azure SQL Serverless (vCore Auto-Pause) | Cosmos DB Serverless (NoSQL) | Cosmos DB Provisioned (manual) |
| ----------- | ----------------------------- | ------------------------------------- | --------------------------------------- | ---------------------------- | ------------------------------ |
| 0           | 5.00                          | 10.00                                 | 1.00                                    | 1.25                         | 20.00                          |
| 5           | 5.10                          | 10.10                                 | 2.02                                    | 2.04                         | 20.20                          |
| 50          | 5.25                          | 12.48                                 | 5.15                                    | 5.25                         | 22.88                          |
| 500         | 11.00                         | 28.00                                 | 17.25                                   | 11.50                        | 33.00                          |
| 5 000       | 24.00                         | 65.00                                 | 54.00                                   | 32.50                        | 62.50                          |
| 50 000      | 56.00                         | 160.00                                | 136.00                                  | 80.00                        | 139.50                         |
| 500 000     | 126.00                        | 360.00                                | 352.00                                  | 252.00                       | 360.00                         |
