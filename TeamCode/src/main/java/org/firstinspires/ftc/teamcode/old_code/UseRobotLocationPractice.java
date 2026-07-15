package org.firstinspires.ftc.teamcode.old_code;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

@Disabled
@TeleOp
public class UseRobotLocationPractice extends OpMode {

    RobotLocationPractice robotLocationPractice = new RobotLocationPractice(0);
    @Override
    public void init() {
        robotLocationPractice.setAngle(0);
        robotLocationPractice.setX(0);
        robotLocationPractice.setY(0);
    }

    @Override
    public void loop() {
        if (gamepad1.a){
            robotLocationPractice.turnRobot(0.1);
        }else if (gamepad1.b){
            robotLocationPractice.turnRobot(-0.1);
        }

        if (gamepad1.dpad_left){
            robotLocationPractice.changeX(0.1);
        }else if (gamepad1.dpad_right){
            robotLocationPractice.changeX(-0.1);
        }

        if (gamepad1.dpad_up){
            robotLocationPractice.changeY(0.1);
        }else if (gamepad1.dpad_down){
            robotLocationPractice.changeY(-0.1);
        }

        telemetry.addData("heading",robotLocationPractice.getHeading());
        telemetry.addData("Angle",robotLocationPractice.getAngle());
        telemetry.addData("X",robotLocationPractice.getX());
        telemetry.addData("Y value",robotLocationPractice.getY());
    }
}
