# TeleOpMovements_NewControl

Italiano sotto ogni sezione, traduzione inglese subito dopo.
Italian below each section, English translation right after.

## Panoramica

Questo OpMode TeleOp per FTC implementa due modalità di controllo: Claudio Mode (due giocatori) e God Mode (un gamepad prende il controllo totale). Si passa dall'una all'altra al volo con una combo di tasti.

## Overview

This FTC TeleOp OpMode implements two control modes: Claudio Mode (two players) and God Mode (one gamepad takes full control). You switch between them on the fly with a button combo.

---

## Claudio Mode (modalità di default)

Gamepad1 guida il robot, gamepad2 controlla i meccanismi (intake, flywheel, servo).

## Claudio Mode (default mode)

Gamepad1 drives the robot, gamepad2 controls the mechanisms (intake, flywheel, servo).

---

## God Mode

Si attiva premendo L3+R3 insieme su uno dei due gamepad. Chi preme la combo diventa il "master" e prende il controllo di guida e meccanismi; l'altro gamepad viene disattivato del tutto. Si esce premendo di nuovo la combo su uno qualsiasi dei due gamepad. La Uster Mode non è disponibile in God Mode, solo in Claudio Mode.

## God Mode

Activated by pressing L3+R3 together on either gamepad. Whoever presses the combo becomes the "master" and takes over both driving and mechanisms; the other gamepad is fully disabled. You exit by pressing the combo again on either gamepad. Uster Mode is not available in God Mode, only in Claudio Mode.

---

## Tasti - Claudio Mode, Gamepad1 (guida)

- Stick sinistro (Y): avanti/indietro (Arcade) o motore SX (Uster)
- Stick destro: sterzo (Arcade) o motore DX (Uster)
- B (cerchio): toggle Uster Mode / Arcade Mode
- X (quadrato): toggle Climbing Mode
- D-pad su/giù: regola la velocità di climbing
- L2/R2: marcia giù/su (scala di potenza)
- L3+R3: attiva God Mode

## Buttons - Claudio Mode, Gamepad1 (driver)

- Left stick (Y): forward/back (Arcade) or left motor (Uster)
- Right stick: steering (Arcade) or right motor (Uster)
- B (circle): toggle Uster Mode / Arcade Mode
- X (square): toggle Climbing Mode
- D-pad up/down: adjust climb speed
- L2/R2: gear down/up (power scale)
- L3+R3: activate God Mode

---

## Tasti - Claudio Mode, Gamepad2 (meccanismi)

- A (croce): sparo, apre i servo e attiva l'intake (solo se il flywheel supera 1800 RPM)
- B (cerchio): toggle servo aperto/chiuso
- Y (triangolo): toggle flywheel idle (900 RPM)
- X (quadrato): toggle flywheel full speed (2000 RPM)
- D-pad su: eject del flywheel se è quasi fermo (RPM < 100), lo spinge indietro a -500 e apre i servo
- R1: intake
- L1: outtake
- L3+R3: attiva God Mode

## Buttons - Claudio Mode, Gamepad2 (operator)

- A (cross): shoot, opens the servos and runs the intake (only if the flywheel is above 1800 RPM)
- B (circle): toggle servo open/closed
- Y (triangle): toggle flywheel idle (900 RPM)
- X (square): toggle flywheel full speed (2000 RPM)
- D-pad up: eject the flywheel if it's nearly stopped (RPM < 100), pushes it backward to -500 and opens the servos
- R1: intake
- L1: outtake
- L3+R3: activate God Mode

---

## Tasti - God Mode (gamepad master)

Stessa disposizione di guida di gamepad1, ma con i meccanismi rimappati per non entrare in conflitto con climbing e regolazione velocità di climbing (che restano su X e D-pad su/giù).

- Stick sinistro/destro: guida Arcade
- A: sparo (solo se flywheel > 1800 RPM)
- B: toggle servo aperto/chiuso
- X: toggle Climbing Mode
- Y: toggle flywheel idle
- D-pad sinistra: toggle flywheel full speed
- D-pad destra: eject flywheel
- D-pad su/giù: regola velocità di climbing
- R1: intake
- L1: outtake, spinge anche il flywheel indietro a -0.5
- L2/R2: marcia giù/su
- L3+R3: disattiva God Mode

## Buttons - God Mode (master gamepad)

Same driving layout as gamepad1, but mechanisms are remapped to avoid conflicting with climbing and climb speed adjustment (which stay on X and D-pad up/down).

- Left/right stick: Arcade drive
- A: shoot (only if flywheel > 1800 RPM)
- B: toggle servo open/closed
- X: toggle Climbing Mode
- Y: toggle flywheel idle
- D-pad left: toggle flywheel full speed
- D-pad right: eject flywheel
- D-pad up/down: adjust climb speed
- R1: intake
- L1: outtake, also pushes flywheel backward at -0.5
- L2/R2: gear down/up
- L3+R3: deactivate God Mode

---

## Logica del flywheel

Tre stati possibili, in ordine di priorità:

1. Full speed (se attivo): sale gradualmente fino a 2000 tramite la rampa di `update()`, passo massimo 16 per ciclo.
2. Idle (se attivo e full speed spento): 900 fisso, senza rampa.
3. Spento: 0, a meno che sia in corso un eject.

L'eject è pensato per liberare palline incastrate: parte solo se il flywheel è quasi fermo e lo spinge in senso opposto per un istante.

Lo sparo (tasto A) controlla `flywheel_left.getVelocity() > 1800` indipendentemente da idle/full speed, così parte solo quando il flywheel è davvero a regime.

## Flywheel logic

Three possible states, in priority order:

1. Full speed (if active): ramps up gradually to 2000 via `update()`, max step of 16 per loop.
2. Idle (if active and full speed is off): fixed at 900, no ramping.
3. Off: 0, unless an eject is in progress.

The eject is meant to clear jammed balls: it only fires if the flywheel is nearly stopped, pushing it in reverse briefly.

Shooting (A button) checks `flywheel_left.getVelocity() > 1800` regardless of idle/full speed, so it only fires once the flywheel is actually up to speed.

---

## Costanti principali

- TARGET_VELOCITY = 2000 (flywheel full speed)
- IDLE_VELOCITY = 900 (flywheel idle)
- SERVO_CLOSE = 0.01
- SERVO_OPEN = 0.22
- SERVO_SHOOT = 0.12
- maxStep = 16 (rampa massima per ciclo)

## Main constants

- TARGET_VELOCITY = 2000 (flywheel full speed)
- IDLE_VELOCITY = 900 (flywheel idle)
- SERVO_CLOSE = 0.01
- SERVO_OPEN = 0.22
- SERVO_SHOOT = 0.12
- maxStep = 16 (max ramp per loop)

---

## Hardware richiesto (nomi di configurazione)

left_motor, right_motor, intake_motor, second_intake_motor, flywheel_left, flywheel_right (DcMotorEx con encoder), servitore_1, servitore_2, climb_motor_int, climb_motor_est.

## Required hardware (configuration names)

left_motor, right_motor, intake_motor, second_intake_motor, flywheel_left, flywheel_right (DcMotorEx with encoder), servitore_1, servitore_2, climb_motor_int, climb_motor_est.

---

## Note

Tutti i toggle (Uster, Climbing, Flywheel Idle, Flywheel Full Speed, Servo, God Mode) usano edge detection standard: scattano una volta sola per pressione, non sfarfallano se il tasto resta premuto.

In God Mode la Uster Mode è esclusa di proposito, si guida solo in Arcade. Il mapping dei meccanismi in God Mode è stato scelto per non sovrapporsi ai comandi di guida/climbing già su gamepad1; se serve un mapping diverso basta cercare i riferimenti a `master.` nel codice.

## Notes

All toggles (Uster, Climbing, Flywheel Idle, Flywheel Full Speed, Servo, God Mode) use standard edge detection: they fire once per press, no flickering if the button stays held.

Uster Mode is intentionally left out of God Mode, driving is Arcade-only there. The God Mode mechanism mapping was chosen to avoid overlapping with the drive/climbing controls already on gamepad1; if a different mapping is needed, just search for `master.` references in the code.