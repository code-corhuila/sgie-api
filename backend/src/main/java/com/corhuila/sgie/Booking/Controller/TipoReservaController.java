package com.corhuila.sgie.Booking.Controller;

import com.corhuila.sgie.Booking.Entity.TipoReserva;
import com.corhuila.sgie.Booking.IService.ITipoReservaService;
import com.corhuila.sgie.common.BaseController;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("v1/api/tipo-reserva")
public class TipoReservaController extends BaseController<TipoReserva, ITipoReservaService> {
    public TipoReservaController(ITipoReservaService service) {
        super(service, "TIPO_RESERVA");
    }
}

