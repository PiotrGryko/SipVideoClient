/**
 * Copyright (C) 2010-2012 Regis Montoya (aka r3gis - www.r3gis.fr)
 * This file is part of CSipSimple.
 *
 *  CSipSimple is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  If you own a pjsip commercial license you can also redistribute it
 *  and/or modify it under the terms of the GNU Lesser General Public License
 *  as an android library.
 *
 *  CSipSimple is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with CSipSimple.  If not, see <http://www.gnu.org/licenses/>.
 */

package sip.piotr.com.sipclient.pjsip.player.impl;


import org.pjsip.pjsua.pjsua;
import org.pjsip.pjsua.pjsuaConstants;

import java.io.IOException;

import sip.piotr.com.sipclient.api.SipCallSession;
import sip.piotr.com.sipclient.api.SipManager;
import sip.piotr.com.sipclient.pjsip.player.IPlayerHandler;
import sip.piotr.com.sipclient.pjsip.service.SipService;

public class SimpleWavPlayerHandler implements IPlayerHandler {

    private final SipCallSession callInfo;
    private final int way;
    private final int playerId;

    public SimpleWavPlayerHandler(SipCallSession callInfo, String filePath, int way) throws SipService.SameThreadException, IOException {
        this.callInfo = callInfo;
        this.way = way;
        
        int[] plId = new int[1];
        int status = pjsua.player_create(pjsua.pj_str_copy(filePath), 1 /* PJMEDIA_FILE_NO_LOOP */,
                plId);

        if (status == pjsuaConstants.PJ_SUCCESS) {
            // Save player
            playerId = plId[0];
        } else {
            throw new IOException("Cannot create player " + status);
        }
    }

    @Override
    public void startPlaying() throws SipService.SameThreadException {

        // Connect player to requested ports
        int wavPort = pjsua.player_get_conf_port(playerId);
        if ((way & SipManager.BITMASK_OUT) == SipManager.BITMASK_OUT) {
            int wavConfPort = callInfo.getConfPort();
            pjsua.conf_connect(wavPort, wavConfPort);
        }
        if ((way & SipManager.BITMASK_IN) == SipManager.BITMASK_IN) {
            pjsua.conf_connect(wavPort, 0);
        }
        // Once connected, start to play
        pjsua.player_set_pos(playerId, 0);
    }

    @Override
    public void stopPlaying() throws SipService.SameThreadException {
        pjsua.player_destroy(playerId);
    }

}
