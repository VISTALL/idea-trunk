/*
 * Copyright (c) 2007-2009, Osmorc Development Team
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright notice, this list
 *       of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright notice, this
 *       list of conditions and the following disclaimer in the documentation and/or other
 *       materials provided with the distribution.
 *     * Neither the name of 'Osmorc Development Team' nor the names of its contributors may be
 *       used to endorse or promote products derived from this software without specific
 *       prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT
 * OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.osmorc.manifest.lang.headerparser;

import org.jetbrains.annotations.NotNull;

/**
 * A match describes how good a header known to a particular header provider matches a given header.
 * The name of the given header may contain typos and so there may be no perfect match. A perfect match will
 * have a Levenshtein distance of 0. Worse matches will have greater Levenshtein distances.
 *
 * @author Robert F. Beeger (robert@beeger.net)
 */
public class HeaderNameMatch implements Comparable<HeaderNameMatch> {
    public HeaderNameMatch(int distance, @NotNull HeaderParserProvider provider) {
        _distance = distance;
        _provider = provider;
    }

    public int getDistance() {
        return _distance;
    }

    public HeaderParserProvider getProvider() {
        return _provider;
    }

    /**
     * Matches are compared baaed on their distance from their Levenshtein distance.
     *
     * @param o
     * @return
     */
    public int compareTo(HeaderNameMatch o) {
        return getDistance() - o.getDistance();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        HeaderNameMatch that = (HeaderNameMatch) o;

        return _distance == that._distance && _provider.equals(that._provider);

    }

    @Override
    public int hashCode() {
        int result = _distance;
        result = 31 * result + _provider.hashCode();
        return result;
    }

    private int _distance;
    private HeaderParserProvider _provider;
}
