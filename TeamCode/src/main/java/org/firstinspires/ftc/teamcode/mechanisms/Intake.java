package org.firstinspires.ftc.teamcode.mechanisms;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

public class Intake {
    private DcMotor upIntakeMotor, upIntakeSlowMotor;
    private Servo servitoreRight, servitoreLeft;

    private static final double SERVO_CLOSE = 0.04;
    private static final double SERVO_OPEN = 0.12;

    public void init(HardwareMap hardwareMap) {
        upIntakeMotor = hardwareMap.get(DcMotor.class, "intake_motor");
        upIntakeSlowMotor = hardwareMap.get(DcMotor.class, "second_intake_motor");

        servitoreRight = hardwareMap.get(Servo.class, "servitore_1");
        servitoreLeft = hardwareMap.get(Servo.class, "servitore_2");

        upIntakeMotor.setDirection(DcMotor.Direction.FORWARD);
        upIntakeSlowMotor.setDirection(DcMotor.Direction.REVERSE);

        servitoreRight.setDirection(Servo.Direction.REVERSE);
        servitoreLeft.setDirection(Servo.Direction.FORWARD);

        upIntakeMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        upIntakeSlowMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

        upIntakeMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        upIntakeSlowMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);

        closeServos();
    }

    public void intake() {
        closeServos();
        upIntakeMotor.setPower(-1);
        upIntakeSlowMotor.setPower(0);
    }

    public void outtake() {
        closeServos();
        upIntakeMotor.setPower(1);
        upIntakeSlowMotor.setPower(1);
    }

    public void shootFeed() {
        openServos();
        upIntakeMotor.setPower(-1);
        upIntakeSlowMotor.setPower(-1);
    }

    public void stop() {
        closeServos();
        upIntakeMotor.setPower(0);
        upIntakeSlowMotor.setPower(0);
    }

    private void openServos() {
        servitoreRight.setPosition(SERVO_OPEN);
        servitoreLeft.setPosition(SERVO_OPEN);
    }

    private void closeServos() {
        servitoreRight.setPosition(SERVO_CLOSE);
        servitoreLeft.setPosition(SERVO_CLOSE);
    }

    public double getPower() { return upIntakeMotor.getPower(); }
}
