import random
import time
from azure.iot.device import IoTHubDeviceClient, Message

# Replace with the actual device connection string from Azure
CONNECTION_STRING = "HostName=iothubparthmoradiya.azure-devices.net;DeviceId=my-device-id-21199434;SharedAccessKey=blDryY62FTFDN9TBE8KO4wnxs+QyycPegqFjjVHHSCM="

# Message template
MSG_TXT = '{{"sensor_id": "{sensor_id}", "temperature": {temperature}, "humidity": {humidity}, "gas_level": {gas_level}, "motion_detected": {motion}}}'

# Edge filtering logic (acts as simplified "Edge Intelligence")
def filter_data(temp, hum, gas, motion):
    return (
        10 <= temp <= 40 and
        20 <= hum <= 80 and
        gas < 300 and
        motion is True
    )

def simulate_sensor_data(sensor_id):
    temperature = round(random.uniform(5, 45), 2)  # Can generate out-of-range
    humidity = random.randint(10, 90)
    gas_level = random.randint(100, 500)  # Simulated air quality (ppm)
    motion_detected = random.choice([True, False])
    return sensor_id, temperature, humidity, gas_level, motion_detected

def main():
    client = IoTHubDeviceClient.create_from_connection_string(CONNECTION_STRING)
    print("Starting IoT simulation with edge intelligence (aligned with research paper)...")

    while True:
        sensor_id = f"sensor_{random.randint(1, 5)}"
        sensor_id, temperature, humidity, gas, motion = simulate_sensor_data(sensor_id)

        if filter_data(temperature, humidity, gas, motion):
            msg_txt_formatted = MSG_TXT.format(
                sensor_id=sensor_id,
                temperature=temperature,
                humidity=humidity,
                gas_level=gas,
                motion=motion
            )
            message = Message(msg_txt_formatted)
            print(f"✅ Sending to Azure: {msg_txt_formatted}")
            client.send_message(message)
        else:
            print(f"⛔ Filtered out: Temp={temperature}, Humidity={humidity}, Gas={gas}, Motion={motion}")

        time.sleep(5)  # Send every 5 seconds

if __name__ == '__main__':
    main()
