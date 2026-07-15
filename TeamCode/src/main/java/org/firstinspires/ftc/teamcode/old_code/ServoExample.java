package org.firstinspires.ftc.teamcode.old_code;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.mechanisms.TestBenchServo;

@Disabled
@TeleOp
public class ServoExample extends OpMode {

    TestBenchServo bench = new TestBenchServo();

    @Override
    public void init() {
        bench.init(hardwareMap);
    }

    @Override
    public void loop() {
        if (gamepad1.a){
            bench.setServoPos(-1.0);
        } else {
            bench.setServoPos(1.0);
        }

        if (gamepad1.b){
            bench.setServoRot(1.0);
        } else {
            bench.setServoRot(0);
        }
    }
}
