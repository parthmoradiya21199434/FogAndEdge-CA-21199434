package org.fog.test.perfeval;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.power.PowerHost;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;
import org.cloudbus.cloudsim.sdn.overbooking.BwProvisionerOverbooking;
import org.cloudbus.cloudsim.sdn.overbooking.PeProvisionerOverbooking;
import org.fog.application.AppEdge;
import org.fog.application.AppLoop;
import org.fog.application.Application;
import org.fog.application.selectivity.FractionalSelectivity;
import org.fog.entities.Actuator;
import org.fog.entities.FogBroker;
import org.fog.entities.FogDevice;
import org.fog.entities.FogDeviceCharacteristics;
import org.fog.entities.Sensor;
import org.fog.entities.Tuple;
import org.fog.placement.Controller;
import org.fog.placement.ModuleMapping;
import org.fog.placement.ModulePlacementMapping;
import org.fog.policy.AppModuleAllocationPolicy;
import org.fog.scheduler.StreamOperatorScheduler;
import org.fog.utils.FogLinearPowerModel;
import org.fog.utils.FogUtils;
import org.fog.utils.distribution.DeterministicDistribution;

public class X21199434 {

    static List<FogDevice> fogDevices = new ArrayList<>();
    static List<Sensor> sensors = new ArrayList<>();
    static List<Actuator> actuators = new ArrayList<>();

    public static void main(String[] args) {
        Log.printLine("Starting Edge-Cloud Environmental Monitoring Simulation...");

        try {
            CloudSim.init(1, Calendar.getInstance(), false);

            FogBroker broker = new FogBroker("broker");
            String appId = "EnvMonitorApp";

            Application app = createApplication(appId, broker.getId());

            FogDevice cloud = createDevice("cloud", 10000, 8000, 10000, 10000, 0, 0.01, 100, 50);
            cloud.setParentId(-1);
            fogDevices.add(cloud);

            FogDevice edgeNode = createDevice("edge-1", 2000, 2048, 5000, 5000, 1, 0.0, 80, 40);
            edgeNode.setParentId(cloud.getId());
            edgeNode.setUplinkLatency(100);
            fogDevices.add(edgeNode);

            FogDevice sensorNode = createDevice("sensor-node-1", 1000, 512, 1000, 1000, 2, 0.0, 50, 20);
            sensorNode.setParentId(edgeNode.getId());
            sensorNode.setUplinkLatency(10);
            fogDevices.add(sensorNode);

            // Add sensors (temperature, humidity)
            Sensor tempSensor = new Sensor("temp-sensor", "TEMP", broker.getId(), appId, new DeterministicDistribution(5));
            tempSensor.setGatewayDeviceId(sensorNode.getId());
            tempSensor.setLatency(1.0);
            sensors.add(tempSensor);

            Sensor humiditySensor = new Sensor("humidity-sensor", "HUMIDITY", broker.getId(), appId, new DeterministicDistribution(5));
            humiditySensor.setGatewayDeviceId(sensorNode.getId());
            humiditySensor.setLatency(1.0);
            sensors.add(humiditySensor);

            // Add actuator (dashboard)
            Actuator dashboard = new Actuator("dashboard", broker.getId(), appId, "DASHBOARD");
            dashboard.setGatewayDeviceId(cloud.getId());
            dashboard.setLatency(1.0);
            actuators.add(dashboard);

            ModuleMapping moduleMapping = ModuleMapping.createModuleMapping();
            moduleMapping.addModuleToDevice("data_collector", sensorNode.getName());
            moduleMapping.addModuleToDevice("edge_filter", edgeNode.getName());
            moduleMapping.addModuleToDevice("cloud_analytics", cloud.getName());

            Controller controller = new Controller("controller", fogDevices, sensors, actuators);
            controller.submitApplication(app, new ModulePlacementMapping(fogDevices, app, moduleMapping));

            CloudSim.startSimulation();
            CloudSim.stopSimulation();
            Log.printLine("Simulation finished.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static FogDevice createDevice(String name, long mips, int ram, long upBw, long downBw, int level,
                                          double ratePerMips, double busyPower, double idlePower) throws Exception {

        List<Pe> peList = new ArrayList<>();
        peList.add(new Pe(0, new PeProvisionerOverbooking(mips)));

        int hostId = FogUtils.generateEntityId();
        long storage = 1000000; int bw = 10000;

        PowerHost host = new PowerHost(hostId,
                new RamProvisionerSimple(ram),
                new BwProvisionerOverbooking(bw),
                storage,
                peList,
                new StreamOperatorScheduler(peList),
                new FogLinearPowerModel(busyPower, idlePower));

        List<Host> hostList = new ArrayList<>(); hostList.add(host);

        FogDeviceCharacteristics characteristics = new FogDeviceCharacteristics(
                "x86", "Linux", "Xen", host, 10.0, 3.0, 0.05, 0.001, 0.0);

        FogDevice device = new FogDevice(name, characteristics,
                new AppModuleAllocationPolicy(hostList),
                new LinkedList<>(), 10, upBw, downBw, 0, ratePerMips);

        device.setLevel(level);
        return device;
    }

    private static Application createApplication(String appId, int userId) {
        Application application = Application.createApplication(appId, userId);

        application.addAppModule("data_collector", 10);
        application.addAppModule("edge_filter", 10);
        application.addAppModule("cloud_analytics", 10);

        application.addAppEdge("TEMP", "data_collector", 1000, 200, "TEMP", Tuple.UP, AppEdge.SENSOR);
        application.addAppEdge("HUMIDITY", "data_collector", 1000, 200, "HUMIDITY", Tuple.UP, AppEdge.SENSOR);

        application.addAppEdge("data_collector", "edge_filter", 1500, 500, "RAW_ENV_DATA", Tuple.UP, AppEdge.MODULE);
        application.addAppEdge("edge_filter", "cloud_analytics", 1500, 1000, "FILTERED_ENV_DATA", Tuple.UP, AppEdge.MODULE);
        application.addAppEdge("cloud_analytics", "DASHBOARD", 500, 100, "VISUALIZED_DATA", Tuple.DOWN, AppEdge.ACTUATOR);

        application.addTupleMapping("data_collector", "TEMP", "RAW_ENV_DATA", new FractionalSelectivity(1.0));
        application.addTupleMapping("data_collector", "HUMIDITY", "RAW_ENV_DATA", new FractionalSelectivity(1.0));
        application.addTupleMapping("edge_filter", "RAW_ENV_DATA", "FILTERED_ENV_DATA", new FractionalSelectivity(0.7));
        application.addTupleMapping("cloud_analytics", "FILTERED_ENV_DATA", "VISUALIZED_DATA", new FractionalSelectivity(1.0));

        final AppLoop loop = new AppLoop(Arrays.asList("data_collector", "edge_filter", "cloud_analytics"));
        application.setLoops(Collections.singletonList(loop));

        return application;
    }
}