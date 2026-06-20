/*
 * =============================================================================
 *
 *   Copyright (c) 2011-2016, The THYMELEAF team (http://www.thymeleaf.org)
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 * =============================================================================
 */
package thymeleafexamples.gtvg.business.entities;


public class GoogleCollaborator {

    private final String email;
    private final String name;
    private final String givenName;
    private final String familyName;
    private final String pictureUrl;


    public GoogleCollaborator(final String email, final String name, final String givenName,
                              final String familyName, final String pictureUrl) {
        super();
        this.email = email;
        this.name = name;
        this.givenName = givenName;
        this.familyName = familyName;
        this.pictureUrl = pictureUrl;
    }


    public String getEmail() {
        return this.email;
    }


    public String getName() {
        return this.name;
    }


    public String getGivenName() {
        return this.givenName;
    }


    public String getFamilyName() {
        return this.familyName;
    }


    public String getPictureUrl() {
        return this.pictureUrl;
    }

}
