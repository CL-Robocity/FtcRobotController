package org.firstinspires.ftc.teamcode.old_code;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;

import org.firstinspires.ftc.teamcode.mechanisms.TestBench;

@Disabled
@TeleOp
public class DCMotorPractice extends OpMode {
    TestBench bench = new TestBench();

    @Override
    public void init() {
        bench.init(hardwareMap);
    }

    @Override
    public void loop() {
        double leftStickY= -gamepad1.left_stick_y;
        bench.setMotorSpeed(leftStickY);

        if (gamepad1.a){
            bench.changeBrakeMode(DcMotor.ZeroPowerBehavior.BRAKE);
        }else if (gamepad1.b){
            bench.changeBrakeMode(DcMotor.ZeroPowerBehavior.FLOAT);
        }
        telemetry.addData("Motor Revs", bench.getMotorRevs());
    }
}
