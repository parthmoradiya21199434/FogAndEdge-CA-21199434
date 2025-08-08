# IoT-Enabled Big Data Analytics Architecture for Edge–Cloud Computing

This project is based on the research paper:

**"An Optimized IoT-Enabled Big Data Analytics Architecture for Edge–Cloud Computing"**  
(IEEE Access, 2022) — authored by Babar et al.

---

## Project Overview

This project simulates an IoT-based environmental monitoring system using **Edge and Cloud computing**.

- Real-time sensor data is filtered at the edge.
- Cleaned data is forwarded to Azure IoT Hub (cloud).
- Final analytics are visualized via dashboards or cloud functions.

The architecture follows a layered approach:

- **Sensor Node** (e.g., Temperature, Humidity sensors, Gas sensors)
- **Edge Gateway** (data filtering)
- **Azure Cloud** (data analytics + visualization)

---

## Technologies Used

- Python 3.9  
- Azure IoT Hub (via Student Account)
- Azure IoT Device SDK (`azure-iot-device`)
- Visual Studio Code (or PyCharm)
- Optional: Eclipse + iFogSim for Fog topology modeling

---

## Requirements

Install all dependencies using:

```bash
pip install -r requirements.txt
