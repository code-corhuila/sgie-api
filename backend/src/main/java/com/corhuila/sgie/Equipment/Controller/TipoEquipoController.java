package com.corhuila.sgie.Equipment.Controller;

import com.corhuila.sgie.Equipment.Entity.TipoEquipo;
import com.corhuila.sgie.Equipment.IService.ITipoEquipoService;
import com.corhuila.sgie.common.BaseController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("v1/api/tipo-equipo")
public class TipoEquipoController extends BaseController<TipoEquipo, ITipoEquipoService> {
    public TipoEquipoController(ITipoEquipoService service) {
        super(service, "TIPO_EQUIPO");
    }
}
