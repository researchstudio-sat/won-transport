/*
 * Copyright 2017  Research Studios Austria Forschungsges.m.b.H.
 *
 *      Licensed under the Apache License, Version 2.0 (the "License");
 *      you may not use this file except in compliance with the License.
 *      You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *      Unless required by applicable law or agreed to in writing, software
 *      distributed under the License is distributed on an "AS IS" BASIS,
 *      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *      See the License for the specific language governing permissions and
 *      limitations under the License.
 */

package won.transport.taxibot.client.entity.Parameter;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@XmlRootElement(name="ERROR")
public class Error extends Parameter implements Serializable {
    private String text;
    private int id;
    private int type; //1= Fatal Error(session closed), 2= Error(interner programmfehler), 3=Warning(Fehleingabe)

    public Error() {
    }

    public Error(String text) {
        this.text = text;
    }

    @XmlAttribute(name = "TEXT")
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @XmlAttribute(name = "ID")
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @XmlAttribute(name = "TYP")
    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String toString(){
        return "ErrorID: '"+ id + "' Text: '"+ text + "' Type: '" + type + "'";
    }
}
