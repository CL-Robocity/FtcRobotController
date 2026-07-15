package org.firstinspires.ftc.teamcode.old_code;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

@Disabled
@TeleOp
public class ThisIsATest extends OpMode {
    public void init() {
        String teamName = "Team Italy";
        int teamNumber = 23172;
        double robotAngle=23.4;
        telemetry.addData("Team Name", teamName);
        telemetry.addData("Team Number", teamNumber);
        telemetry.addData("Robot Angle", robotAngle);
        telemetry.update();

    }
    public void loop() {

    }
}
