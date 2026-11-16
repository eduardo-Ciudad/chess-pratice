package com.eduar.chess.adapter.in.web;

import com.eduar.chess.adapter.in.web.dto.AddLineRequest;
import com.eduar.chess.adapter.in.web.dto.CreateRepertoireRequest;
import com.eduar.chess.adapter.in.web.dto.CreateRepertoireResponse;
import com.eduar.chess.adapter.in.web.dto.ImportPgnRequest;
import com.eduar.chess.application.port.in.AddLineCommand;
import com.eduar.chess.application.port.in.AddLineResult;
import com.eduar.chess.application.port.in.AddLineUseCase;
import com.eduar.chess.application.port.in.CreateRepertoireCommand;
import com.eduar.chess.application.port.in.CreateRepertoireUseCase;
import com.eduar.chess.application.port.in.GetRepertoireUseCase;
import com.eduar.chess.application.port.in.ImportPgnCommand;
import com.eduar.chess.application.port.in.ImportPgnResult;
import com.eduar.chess.application.port.in.ImportPgnUseCase;
import com.eduar.chess.application.port.in.ListRepertoiresUseCase;
import com.eduar.chess.application.port.in.RepertoireSummary;
import com.eduar.chess.application.port.in.RepertoireView;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/repertoires")
public class RepertoireController {

    private final CreateRepertoireUseCase createRepertoire;
    private final ListRepertoiresUseCase listRepertoires;
    private final GetRepertoireUseCase getRepertoire;
    private final AddLineUseCase addLine;
    private final ImportPgnUseCase importPgn;

    public RepertoireController(CreateRepertoireUseCase createRepertoire,
                                ListRepertoiresUseCase listRepertoires,
                                GetRepertoireUseCase getRepertoire,
                                AddLineUseCase addLine,
                                ImportPgnUseCase importPgn) {
        this.createRepertoire = createRepertoire;
        this.listRepertoires = listRepertoires;
        this.getRepertoire = getRepertoire;
        this.addLine = addLine;
        this.importPgn = importPgn;
    }

    @PostMapping
    public ResponseEntity<CreateRepertoireResponse> create(@Valid @RequestBody CreateRepertoireRequest request) {
        UUID id = createRepertoire.create(new CreateRepertoireCommand(request.name(), request.side()));
        return ResponseEntity
                .created(URI.create("/api/repertoires/" + id))
                .body(new CreateRepertoireResponse(id));
    }

    @GetMapping
    public List<RepertoireSummary> list() {
        return listRepertoires.list();
    }

    @GetMapping("/{id}")
    public RepertoireView get(@PathVariable UUID id) {
        return getRepertoire.get(id);
    }

    @PostMapping("/{id}/lines")
    public AddLineResult addLine(@PathVariable UUID id, @Valid @RequestBody AddLineRequest request) {
        return addLine.addLine(new AddLineCommand(id, request.moves()));
    }

    @PostMapping("/{id}/pgn")
    public ImportPgnResult importPgn(@PathVariable UUID id, @Valid @RequestBody ImportPgnRequest request) {
        return importPgn.importPgn(new ImportPgnCommand(id, request.pgn()));
    }
}
