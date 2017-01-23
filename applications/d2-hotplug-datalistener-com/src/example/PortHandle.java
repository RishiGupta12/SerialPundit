/*
 * This file is part of SerialPundit.
 * 
 * Copyright (C) 2014-2016, Rishi Gupta. All rights reserved.
 *
 * The SerialPundit is DUAL LICENSED. It is made available under the terms of the GNU Affero 
 * General Public License (AGPL) v3.0 for non-commercial use and under the terms of a commercial 
 * license for commercial use of this software. 
 * 
 * The SerialPundit is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package example;

public final class PortHandle {

    private volatile long comPortHandle;

    public PortHandle(long comPortHandle) {
        this.comPortHandle = comPortHandle;
    }

    public long getComPortHandle() {
        return comPortHandle;
    }

    public void setComPortHandle(long comPortHandle) {
        this.comPortHandle = comPortHandle;
    }
}
