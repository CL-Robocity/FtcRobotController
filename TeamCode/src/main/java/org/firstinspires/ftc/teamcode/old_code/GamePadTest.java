package org.firstinspires.ftc.teamcode.old_code;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

@Disabled
@TeleOp
public class GamePadTest extends OpMode {

    @Override
    public void init() {

    }

    @Override
    public void loop() {
        double speedForwardL = gamepad1.left_stick_y * (-1);
        double speedForwardR = gamepad1.right_stick_y * (-1);

        telemetry.addData("left x",gamepad1.left_stick_x);
        telemetry.addData("left y",speedForwardL);

        telemetry.addData("right x",gamepad1.right_stick_x);
        telemetry.addData("right y",speedForwardR);

        telemetry.addData("\na",gamepad1.a);
        telemetry.addData("b", gamepad1.b);
    }
}
