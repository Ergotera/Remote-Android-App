package ca.ergotera.remote_ir.serial_com;

/**
 * A command listener is a handler of communication received from USB Serial.
 *
 * @author Renaud Varin (renaud.varin.1@ens.etsmtl.ca)
 * Copyright 2018, Ergotera Technologies, All rights reserved.
 */
public interface CommandListener {
    void external_button_pressed (int button_id);
    void recorded_IR_code (String code_data);
    void handle_error (String error);
    void ping_response (String ping);
    void state_set_success (String current_state);
    void send_IR_success(String sent_ir);
}