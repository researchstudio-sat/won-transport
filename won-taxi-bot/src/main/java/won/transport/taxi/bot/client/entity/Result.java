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

package won.transport.taxi.bot.client.entity;

import won.transport.taxi.bot.client.entity.Parameter.*;
import won.transport.taxi.bot.client.entity.Parameter.Error;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.List;

@XmlRootElement(name = "RESULT")
public class Result implements Serializable{

    private List<Parameter> parameter;
    private Error error;

    public Result() {
    }

    @XmlElementWrapper(name = "PARAMETER")
    @XmlElements({
            @XmlElement(name="SERVICEID", type=ServiceId.class),
            @XmlElement(name="SERVICENAME", type=ServiceName.class),
            @XmlElement(name="SERVICEPHONE", type=ServicePhone.class),
            @XmlElement(name="ARRIVALMINUTES", type=ArrivalMinutes.class),
            @XmlElement(name="DISPLAYTEXT", type=DisplayText.class),
            @XmlElement(name="PRICE", type=Price.class)
    })
    public List<Parameter> getParameter() {
        return parameter;
    }

    public void setParameter(List<Parameter> parameter) {
        this.parameter = parameter;
    }

    @XmlElement(name = "ERROR")
    public Error getError() {
        return error;
    }

    public void setError(Error error) {
        this.error = error;
    }
}
