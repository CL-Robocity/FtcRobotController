package org.firstinspires.ftc.teamcode.old_code;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

@Disabled
@TeleOp
public class Method extends OpMode {

    @Override
    public void init() {

    }
    double squareInput(double input){
        double output = input * input;
        if (input<0){
            output*=(-1);
        }
        return output;
    }
    @Override
    public void loop() {
        double yAxis = gamepad1.left_stick_y;
        telemetry.addData("yAxis normal",yAxis);

        yAxis=squareInput(yAxis);
        telemetry.addData("YAxis Squared", yAxis);
    }
}
