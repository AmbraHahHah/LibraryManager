package com.library.prog.service;

import com.library.prog.dto.request.EditorRequest;
import com.library.prog.dto.response.EditorResponse;
import com.library.prog.model.Editor;
import com.library.prog.repository.EditorRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EditorService {

	private final EditorRepository editorRepository;

	public List<EditorResponse> findAll() {
		return editorRepository.findAll().stream().map(this::toResponse).toList();
	}

	public EditorResponse findById(UUID id) {
		return editorRepository
			.findById(id)
			.map(this::toResponse)
			.orElseThrow(() -> new EntityNotFoundException("Editor not found: " + id));
	}

	public EditorResponse create(EditorRequest request) {
		var editor = Editor
			.builder()
			.name(request.name())
			.address(request.address())
			.email(request.email())
			.country(request.country())
			.build();
		return toResponse(editorRepository.save(editor));
	}

	public EditorResponse update(UUID id, EditorRequest request) {
		var editor = editorRepository
			.findById(id)
			.orElseThrow(() -> new EntityNotFoundException("Editor not found: " + id));
		editor.setName(request.name());
		editor.setAddress(request.address());
		editor.setEmail(request.email());
		editor.setCountry(request.country());
		return toResponse(editorRepository.save(editor));
	}

	public void delete(UUID id) {
		if (!editorRepository.existsById(id)) {
			throw new EntityNotFoundException("Editor not found: " + id);
		}
		editorRepository.deleteById(id);
	}

	private EditorResponse toResponse(Editor editor) {
		return EditorResponse
			.builder()
			.id(editor.getId())
			.name(editor.getName())
			.address(editor.getAddress())
			.email(editor.getEmail())
			.country(editor.getCountry())
			.build();
	}
}
