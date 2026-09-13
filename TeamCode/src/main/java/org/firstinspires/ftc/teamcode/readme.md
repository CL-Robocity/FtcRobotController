# TeleOpMovements_NewControl

Italiano sotto ogni sezione, traduzione inglese subito dopo.
Italian below each section, English translation right after.

---

## 1. Panoramica del progetto

Questo OpMode TeleOp per FTC implementa due modalità di controllo: **Claudio Mode** (due giocatori, di default) e **God Mode** (un solo gamepad prende il controllo totale). Si passa dall'una all'altra al volo con la combo **L3+R3**, premibile da entrambi i gamepad.

Il robot gestisce: guida (Arcade o Uster), cambio marcia a step, un sistema di intake/outtake a doppio motore, un flywheel a doppia velocità con controllo di sicurezza prima dello sparo, due servo "servitori" per il rilascio delle palline, uno slider a due posizioni fisse, un sistema di climbing a velocità regolabile, e un tentativo (non funzionante) di feedback sonoro.

## 1. Project Overview

This FTC TeleOp OpMode implements two control modes: **Claudio Mode** (two players, default) and **God Mode** (one gamepad takes full control). You switch between them on the fly with the **L3+R3** combo, pressable from either gamepad.

The robot handles: driving (Arcade or Uster), stepped gear shifting, a dual-motor intake/outtake system, a dual-speed flywheel with a safety check before shooting, two "servitore" servos for releasing balls, a two-position slider, an adjustable-speed climbing system, and an (currently non-functional) attempt at sound feedback.

---

## 2. Changelog Hardware/Software

Changelog completo, in ordine cronologico, ricostruito dai commenti in testa al file sorgente. Ogni versione è cumulativa rispetto alla precedente.

Full changelog, in chronological order, reconstructed from the comments at the top of the source file. Each version is cumulative on top of the previous one.

### 3/9/26

**SW:** aggiunto controllo indipendente dell'apertura servo; aggiunta rotazione dello slowintake motor durante l'intake; aggiunta rotazione inversa del flywheel durante l'outtake; modificato l'utilizzo della idle velocity (rimane nel codice ma non più necessaria).
**HW:** modificata l'altezza del primo rullo di intake; aggiunti due pezzi per il blocco/incastro delle palline sia sul buco centrale superiore che sui buchi laterali del flywheel; prima prova dello slider.

**SW:** added independent open-servo control; added slowintake motor rotation during intake; added flywheel reverse rotation during outtake; modified idle-velocity usage (still in the code but no longer needed).
**HW:** changed the height of the first intake roller; added two pieces to block/jam balls both on the central top hole and the side holes of the flywheel; first slider prototype test.

### 4/9/26

**SW:** aggiunto un controllo unico prima dello sparo a 1700 giri; implementate tutte le funzioni sia in God Mode che in Claudio Mode; modificate le velocità di outtake e flywheel a sparo; implementati i servo slider (slider_left, slider_right) e il relativo handle; implementate le posizioni SOUT 0.22 e SIN 0.00. **Refactor**: la logica di meccanismi, climbing e guida (duplicata quasi identica tra le due modalità) è stata spostata in metodi condivisi unici (`handleMechanisms`, `handleClimbing`, `driveArcade`, `applyGearAndDrive`), chiamati da entrambe le modalità passando i tasti giusti — nessun cambio di comportamento a runtime, solo di organizzazione del codice.
**HW:** aggiunto un piccolo pezzo di policarbonato sotto l'intake motor per evitare l'incastro delle palline; i pezzi di appoggio al muro antibattuta sono stati portati alla misura massima per contenere le palline sotto il canestro, poi riportati indietro; implementazione fisica dello slider.

**SW:** added a single check before shooting at 1700 rpm; implemented all functions in both God Mode and Claudio Mode; modified outtake/flywheel shooting speeds; implemented the slider servos (slider_left, slider_right) and their handle; implemented SOUT 0.22 and SIN 0.00 positions. **Refactor**: mechanism/climbing/driving logic (near-identical duplicated code between the two modes) was moved into unique shared methods (`handleMechanisms`, `handleClimbing`, `driveArcade`, `applyGearAndDrive`), called by both modes with the right buttons passed in — no runtime behavior change, only code organization.
**HW:** added a small polycarbonate piece under the intake motor to prevent ball jamming; the anti-rebound wall spacer pieces were moved to maximum size to try to contain balls under the basket, then moved back; physical slider implementation.

### 7/9/26

**SW:** nessuna modifica.
**HW:** alzati rulli e motori per evitare l'incastro delle palline in outtake.

**SW:** no changes.
**HW:** raised rollers and motors to avoid ball jamming during outtake.

### 9/9/26 (prima modifica / first update)

**HW:** rivisitazione storica dell'intake, con aggiunta dello slider e altre piccole modifiche.

**HW:** historic revisit of the intake, with the slider added and other minor tweaks.

### 9/9/26 (seconda modifica / second update)

**SW:** rese completamente parametriche, per la rimappatura del controller, tutte le funzioni `handle...`; tentativo di implementazione dei suoni (non funzionante, vedi sezione SoundPlayer).
**HW:** aggiustate le altezze di flywheel e intake; modificata la rampa con l'aggiunta di una discesa; creata una rete sopra l'intake.

**SW:** made all `handle...` functions fully parametric, for controller remapping; attempted a sound implementation (not working, see the SoundPlayer section).
**HW:** adjusted flywheel and intake heights; modified the ramp adding a descent; created a net above the intake.

### 12/9/26

**SW:** aggiunta `handleSlider` con controllo di posizione tramite i trigger analogici dell'operatore: R2 incrementa la posizione verso SOUT, L2 la decrementa verso SIN, nessun trigger premuto = posizione bloccata all'ultimo valore. Aggiunta telemetria della posizione slider in tempo reale. *(Questo comportamento analogico è stato poi sostituito il giorno seguente, vedi 13/9 e nota nella sezione Slider.)*
**HW:** slider finito ma non funzionante insieme all'intake; tentativo di sparo in retromarcia con il vecchio drivetrain.

**SW:** added `handleSlider` with position control via the operator's analog triggers: R2 increments the position toward SOUT, L2 decrements it toward SIN, no trigger held = position locked at the last value. Added real-time slider position telemetry. *(This analog behavior was replaced the following day, see 13/9 and the note in the Slider section.)*
**HW:** slider finished but not working together with the intake; attempted reverse-direction shooting with the old drivetrain.

### 13/9/26 (versione attuale / current version)

**SW:** aggiunto un controllo di sicurezza per il flywheel legato all'outtake (vedi sezione Flywheel); modificati SIN e SOUT per le nuove lunghezze dei servo e **lo slider è tornato ad essere un toggle** (rimossa la funzione ausiliaria a trigger analogici introdotta il giorno prima); tutte le funzioni sono state inserite dentro `if (fullController)` in modo da avere due mappe controlli distinte e leggibili per God Mode e Claudio Mode.
**HW:** aggiunto plexiglass frontale per la chiusura parziale dell'intake anche da aperto (manca ancora la rete); abbassato uno dei 4 pezzi della rampa per arrampicarsi meglio; rifatti gli zero di entrambi i servo dello slider.

**SW:** added a flywheel safety check tied to outtake (see the Flywheel section); modified SIN and SOUT for the new servo lengths and **the slider went back to being a toggle** (removed the analog-trigger auxiliary function introduced the day before); all functions were moved inside `if (fullController)` to give God Mode and Claudio Mode two distinct, readable control maps.
**HW:** added a front plexiglass panel for partial intake closure even when open (net still missing); lowered one of the 4 ramp pieces for better climbing; redid the zero position of both slider servos.

### Riepilogo modifiche di valore rispetto alla V3 del README / Summary of value changes vs README V3

| Elemento / Item | V3 (documentato prima) | Attuale / Current |
|---|---|---|
| Soglia `velocityok` | > 1700 RPM | **> 1750 RPM** |
| Slider | toggle (X / touchpad) | toggle (X / touchpad) — invariato, dopo una parentesi a trigger analogici il 12/9 poi annullata |
| Schermata di init | — | "Jolly Roger" rimosso (metodo presente ma non più chiamato), messaggio semplificato |
| SoundPlayer | non menzionato | import commentato, tentativo non riuscito |

---

## 3. Mappatura Comandi

### Claudio Mode — Gamepad 1 (Guida / Driver)

| Comando | Funzione |
|---|---|
| Stick sinistro (Y) | Avanti/indietro (Arcade) oppure motore SX (Uster) |
| Stick destro | Sterzo (Arcade) oppure motore DX (Uster) |
| B (cerchio) | Toggle Uster Mode / Arcade Mode |
| X (quadrato) | Toggle Climbing Mode |
| D-pad su / giù | Aumenta / diminuisce la velocità di climbing (±0.1, range -0.9…0.9) |
| L2 / R2 | Marcia giù / su (scala di potenza, step 0.25, range 0.25…1.0) |
| L3+R3 | Attiva God Mode (master = gamepad1) |

### Claudio Mode — Gamepad 1 (Driver)

| Control | Function |
|---|---|
| Left stick (Y) | Forward/back (Arcade) or left motor (Uster) |
| Right stick | Steering (Arcade) or right motor (Uster) |
| B (circle) | Toggle Uster Mode / Arcade Mode |
| X (square) | Toggle Climbing Mode |
| D-pad up / down | Increase / decrease climb speed (±0.1, range -0.9…0.9) |
| L2 / R2 | Gear down / up (power scale, step 0.25, range 0.25…1.0) |
| L3+R3 | Activate God Mode (master = gamepad1) |

### Claudio Mode — Gamepad 2 (Meccanismi / Operator)

| Comando | Funzione |
|---|---|
| A (croce) | Sparo: porta i servo a SERVO_SHOOT e attiva l'intake a -0.8 (solo se `velocityok` è vera) |
| B (cerchio) | Toggle servo aperto/chiuso |
| X (quadrato) | Toggle slider aperto/chiuso |
| Y (triangolo) | Toggle flywheel Full Speed (2000 RPM) |
| Touchpad | Toggle flywheel Idle (900 RPM) |
| R1 | Intake |
| L1 | Outtake "sicuro" (vedi sezione Flywheel/Outtake) |
| L3+R3 | Attiva God Mode (master = gamepad2) |

### Claudio Mode — Gamepad 2 (Operator)

| Control | Function |
|---|---|
| A (cross) | Shoot: moves servos to SERVO_SHOOT and runs intake at -0.8 (only if `velocityok` is true) |
| B (circle) | Toggle servo open/closed |
| X (square) | Toggle slider open/closed |
| Y (triangle) | Toggle flywheel Full Speed (2000 RPM) |
| Touchpad | Toggle flywheel Idle (900 RPM) |
| R1 | Intake |
| L1 | "Safe" outtake (see Flywheel/Outtake section) |
| L3+R3 | Activate God Mode (master = gamepad2) |

### God Mode (gamepad master, attivato con L3+R3)

Un solo gamepad controlla guida e meccanismi. La disposizione ricalca il Gamepad 1 per la guida, ma i meccanismi sono rimappati per non entrare in conflitto con climbing (che resta su X e D-pad su/giù).

| Comando | Funzione |
|---|---|
| Stick sinistro/destro | Guida Arcade (Uster Mode non disponibile) |
| A | Sparo (richiede `velocityok`) |
| B | Toggle servo aperto/chiuso |
| X | Toggle Climbing Mode |
| Y | Toggle flywheel Full Speed |
| R3 (stick destro premuto) | Toggle flywheel Idle |
| Touchpad | Toggle slider aperto/chiuso |
| D-pad su / giù | Regola velocità di climbing |
| R1 | Intake |
| L1 | Outtake "sicuro" |
| L2 / R2 | Marcia giù / su |
| L3+R3 | Disattiva God Mode |

### God Mode (master gamepad, activated with L3+R3)

A single gamepad controls both driving and mechanisms. The layout mirrors Gamepad 1 for driving, but mechanisms are remapped to avoid conflicting with climbing (which stays on X and D-pad up/down).

| Control | Function |
|---|---|
| Left/right stick | Arcade drive (Uster Mode unavailable) |
| A | Shoot (requires `velocityok`) |
| B | Toggle servo open/closed |
| X | Toggle Climbing Mode |
| Y | Toggle flywheel Full Speed |
| R3 (right stick button) | Toggle flywheel Idle |
| Touchpad | Toggle slider open/closed |
| D-pad up / down | Adjust climb speed |
| R1 | Intake |
| L1 | "Safe" outtake |
| L2 / R2 | Gear down / up |
| L3+R3 | Deactivate God Mode |

---

## 4. Sistemi Principali

### 4.1 Guida: Arcade vs Uster

L'Arcade Drive (`driveArcade`) calcola `leftPower`/`rightPower` da throttle + sterzo, normalizzando se la somma supera 1.0. È l'unica modalità disponibile in God Mode. La Uster Mode (doppio stick stile tank, un motore per stick) esiste solo in Claudio Mode ed è attivabile con B su gamepad1. In entrambi i casi, `applyGearAndDrive` applica poi la marcia (`scale`) e invia la potenza ai motori.

The Arcade Drive (`driveArcade`) computes `leftPower`/`rightPower` from throttle + steering, normalizing if the sum exceeds 1.0. It's the only mode available in God Mode. Uster Mode (tank-style dual stick, one motor per stick) exists only in Claudio Mode and is toggled with B on gamepad1. In both cases, `applyGearAndDrive` then applies the gear (`scale`) and sends power to the motors.

### 4.2 Flywheel a doppia velocità e sicurezza di sparo

Tre stati possibili, in ordine di priorità:

1. **Full Speed** (se attivo): sale gradualmente fino a `TARGET_VELOCITY` (2000) tramite la rampa del metodo `update()`, passo massimo 16 per ciclo, per evitare sbalzi di corrente.
2. **Idle** (se attivo e Full Speed spento): velocità fissa `IDLE_VELOCITY` (900), senza rampa.
3. **Spento**: velocità 0, a meno che sia in corso un outtake con flywheel fermo (vedi sotto).

La variabile `velocityok` abilita lo sparo (tasto A): diventa vera solo quando **entrambi** i flywheel superano **1750 RPM** con Full Speed attivo, e resta vera finché Full Speed non viene disattivato (si azzera immediatamente allo spegnimento). Lo sparo non guarda più la velocità istantanea al momento della pressione di A, ma questa variabile calcolata ogni ciclo.

**Outtake "sicuro":** tenendo L1 (o R1/L1 a seconda del gamepad usato per i meccanismi), il comportamento dipende dallo stato del flywheel:
- Flywheel **fermo** (Idle e Full Speed entrambi spenti, velocità < 50 RPM su entrambi i motori): l'intake va in reverse (0.4 / 0.7) e il flywheel viene spinto a **-600** per aiutare a liberare palline incastrate.
- Flywheel **in moto** (Idle o Full Speed attivi, oppure velocità > 50 RPM): viene azionato solo lo slowintake a 0.9, senza toccare il flywheel — questo evita di lanciare palline in direzione sbagliata se si preme outtake per errore mentre il flywheel è a regime.

Three possible states, in priority order:

1. **Full Speed** (if active): ramps up gradually to `TARGET_VELOCITY` (2000) via the `update()` method, max step 16 per cycle, to avoid current spikes.
2. **Idle** (if active and Full Speed off): fixed at `IDLE_VELOCITY` (900), no ramping.
3. **Off**: 0, unless an outtake is in progress with the flywheel stopped (see below).

The `velocityok` variable enables shooting (A button): it becomes true only when **both** flywheels exceed **1750 RPM** with Full Speed active, and stays true until Full Speed is turned off (resets immediately when it is). Shooting no longer checks instantaneous velocity at the moment A is pressed, but this variable, recomputed every cycle.

**"Safe" outtake:** holding L1 (or R1/L1 depending on which gamepad handles mechanisms), behavior depends on flywheel state:
- Flywheel **stopped** (Idle and Full Speed both off, velocity < 50 RPM on both motors): intake reverses (0.4 / 0.7) and the flywheel is pushed to **-600** to help clear jammed balls.
- Flywheel **spinning** (Idle or Full Speed active, or velocity > 50 RPM): only the slow intake runs at 0.9, without touching the flywheel — this prevents shooting balls the wrong way if outtake is pressed by mistake while the flywheel is up to speed.

### 4.3 Slider

Due servo (`slider_dx` → `sliderRight`, `slider_sx` → `sliderLeft`, quest'ultimo con direzione invertita) controllati come **toggle** tra due posizioni fisse: `SIN` (dentro, 0.86) e `SOUT` (fuori, 0.0). Attivabile con X su gamepad2 in Claudio Mode, o con il touchpad del master in God Mode.

> ⚠️ **Nota storica:** il 12/9 era stato provato un controllo analogico (R2 per aprire, L2 per chiudere, con posizione intermedia libera), poi abbandonato il giorno dopo tornando al toggle a due posizioni. Il commento Javadoc sopra il metodo `handleSlider` nel codice descrive ancora il vecchio comportamento analogico: è un residuo di documentazione non aggiornato, l'implementazione reale è quella a toggle descritta qui.
>
> ⚠️ **Bug noto:** la variabile `sliderPosition`, mostrata in telemetria ("Slider position"), non viene mai aggiornata dal toggle e resta sempre al valore iniziale (`SIN`, 0.86) indipendentemente dallo stato reale dello slider. La telemetria di posizione va quindi ignorata o corretta in una prossima revisione.

Two servos (`slider_dx` → `sliderRight`, `slider_sx` → `sliderLeft`, the latter reversed) controlled as a **toggle** between two fixed positions: `SIN` (in, 0.86) and `SOUT` (out, 0.0). Toggled with X on gamepad2 in Claudio Mode, or the master's touchpad in God Mode.

> ⚠️ **Historical note:** on 12/9 an analog control was tried (R2 to open, L2 to close, with a free intermediate position), then abandoned the next day in favor of the two-position toggle. The Javadoc comment above the `handleSlider` method in the code still describes the old analog behavior: this is stale documentation left in the code, the real implementation is the toggle described here.
>
> ⚠️ **Known bug:** the `sliderPosition` variable, shown in telemetry ("Slider position"), is never updated by the toggle and stays at its initial value (`SIN`, 0.86) regardless of the slider's actual state. The position telemetry should be ignored or fixed in a future revision.

### 4.4 Climbing

Toggle attivabile con X (stesso tasto su driver in entrambe le modalità, condiviso tramite `handleClimbing`). Quando attivo, entrambi i motori di climbing (`climb_motor_int`, `climb_motor_est`) girano a `climbVelocity` (default 0.5, regolabile con D-pad su/giù in step di 0.1, range -0.9…0.9). Quando disattivato, potenza a 0.

Toggle activated with X (same button on the driver in both modes, shared via `handleClimbing`). When active, both climbing motors (`climb_motor_int`, `climb_motor_est`) run at `climbVelocity` (default 0.5, adjustable with D-pad up/down in 0.1 steps, range -0.9…0.9). When off, power is 0.

### 4.5 Servomotori (servitori)

`servitoreRight` e `servitoreLeft` sono i servo che trattengono/rilasciano le palline. Hanno tre posizioni possibili gestite da costanti: `SERVO_CLOSE` (0.0), `SERVO_OPEN` (0.2, usata durante intake/outtake a seconda del toggle) e `SERVO_SHOOT` (0.2, usata solo durante lo sparo). Il toggle aperto/chiuso (B) determina quale delle due posizioni "di riposo" viene usata fuori dallo sparo.

`servitoreRight` and `servitoreLeft` are the servos that hold/release balls. They have three possible positions managed by constants: `SERVO_CLOSE` (0.0), `SERVO_OPEN` (0.2, used during intake/outtake depending on the toggle) and `SERVO_SHOOT` (0.2, used only while shooting). The open/closed toggle (B) determines which of the two "resting" positions is used outside of shooting.

### 4.6 SoundPlayer (sperimentale, non funzionante)

Il 9/9 è stato tentato l'utilizzo di `com.qualcomm.ftccommon.SoundPlayer` per aggiungere feedback sonoro (ad es. per confermare lo sparo o il cambio modalità), ma il tentativo non ha funzionato: nel codice attuale l'import è commentato (`//import com.qualcomm.ftccommon.SoundPlayer;`) e non c'è alcuna chiamata attiva al sistema audio. Al momento il robot non produce alcun feedback sonoro; questa parte resta da riprendere in una futura iterazione.

On 9/9 an attempt was made to use `com.qualcomm.ftccommon.SoundPlayer` to add audio feedback (e.g. to confirm shooting or a mode switch), but the attempt didn't work: in the current code the import is commented out (`//import com.qualcomm.ftccommon.SoundPlayer;`) and there is no active call to the audio system. The robot currently produces no sound feedback; this remains to be picked up in a future iteration.

---

## 5. Costanti principali

| Costante / Constant | Valore / Value | Note |
|---|---|---|
| `TARGET_VELOCITY` | 2000 | Flywheel Full Speed |
| `IDLE_VELOCITY` | 900 | Flywheel Idle |
| `SERVO_CLOSE` | 0.0 | Posizione servo chiusa |
| `SERVO_OPEN` | 0.2 | Posizione servo aperta (intake/outtake) |
| `SERVO_SHOOT` | 0.2 | Posizione servo durante sparo |
| `SIN` | 0.86 | Slider dentro |
| `SOUT` | 0.0 | Slider fuori |
| `scale` (iniziale) | 0.75 | Marcia di partenza, range 0.25–1.0 |
| `climbVelocity` (iniziale) | 0.5 | Range -0.9…0.9 |
| `maxStep` (in `update()`) | 16 | Rampa massima flywheel per ciclo |
| Soglia `velocityok` | > 1750 RPM | Su entrambi i flywheel, con Full Speed attivo |
| Soglia outtake sicuro | < 50 RPM | Su entrambi i flywheel, per abilitare il -600 |

---

## 6. Hardware richiesto (nomi di configurazione)

`left_motor`, `right_motor`, `intake_motor`, `second_intake_motor`, `flywheel_left`, `flywheel_right` (DcMotorEx con encoder), `servitore_1` (→ servitoreRight, REVERSE), `servitore_2` (→ servitoreLeft, FORWARD), `slider_dx` (→ sliderRight, FORWARD), `slider_sx` (→ sliderLeft, REVERSE), `climb_motor_int`, `climb_motor_est`.

Required hardware (configuration names): `left_motor`, `right_motor`, `intake_motor`, `second_intake_motor`, `flywheel_left`, `flywheel_right` (DcMotorEx with encoder), `servitore_1` (→ servitoreRight, REVERSE), `servitore_2` (→ servitoreLeft, FORWARD), `slider_dx` (→ sliderRight, FORWARD), `slider_sx` (→ sliderLeft, REVERSE), `climb_motor_int`, `climb_motor_est`.

---

## 7. Note generali e problemi noti

Italiano:

- Tutti i toggle (Uster, Climbing, Flywheel Idle, Flywheel Full Speed, Servo, Slider, God Mode) usano edge detection standard: scattano una volta sola per pressione, non sfarfallano se il tasto resta premuto.
- In God Mode la Uster Mode è esclusa di proposito, si guida solo in Arcade.
- La schermata di init è stata semplificata: il "Jolly Roger" mostrato in passato è stato rimosso dalla chiamata (il metodo esiste ancora nel codice ma non viene più invocato).
- Il commento Javadoc di `handleSlider` nel codice sorgente descrive ancora il vecchio comportamento a trigger analogici (12/9): va aggiornato per riflettere il toggle attuale.
- La telemetria "Slider position" mostra un valore statico (`sliderPosition`, mai aggiornato) e non riflette lo stato reale dello slider: da correggere.
- Il sistema SoundPlayer è presente solo come tentativo, con import disattivato: nessun suono viene attualmente riprodotto dal robot.

English:

- All toggles (Uster, Climbing, Flywheel Idle, Flywheel Full Speed, Servo, Slider, God Mode) use standard edge detection: they fire once per press, no flickering if the button stays held.
- Uster Mode is intentionally excluded from God Mode; driving is Arcade-only there.
- The init screen was simplified: the previously shown "Jolly Roger" was removed from the call (the method is still in the code but is no longer invoked).
- The Javadoc comment on `handleSlider` in the source still describes the old analog-trigger behavior (12/9): it should be updated to reflect the current toggle.
- The "Slider position" telemetry shows a static value (`sliderPosition`, never updated) and does not reflect the slider's real state: this should be fixed.
- The SoundPlayer system is present only as an attempt, with the import disabled: no sound is currently played by the robot.