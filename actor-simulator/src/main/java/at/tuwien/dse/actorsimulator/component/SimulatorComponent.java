package at.tuwien.dse.actorsimulator.component;

import at.tuwien.dse.actorsimulator.service.SimulatorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;

import javax.annotation.ManagedBean;
import javax.inject.Inject;

@ManagedBean
public class SimulatorComponent {

    @Autowired
    private SimulatorService simulatorService;

}
