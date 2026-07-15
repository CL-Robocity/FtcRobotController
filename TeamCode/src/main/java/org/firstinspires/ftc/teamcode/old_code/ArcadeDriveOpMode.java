package org.firstinspires.ftc.teamcode.old_code;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.mechanisms.ArcadeDrive;

@Disabled
@TeleOp(name = "Arcade_Drive_Test", group = "Competition")
public class ArcadeDriveOpMode extends OpMode {

    ArcadeDrive drive = new ArcadeDrive();
    double throttle;
    double spin;

    @Override
    public void init() {
        drive.init(hardwareMap);
    }

    @Override
    public void loop() {
        throttle = -gamepad1.left_stick_y;
        spin = gamepad1.left_stick_x;

        drive.drive(throttle,spin);
    }
}
