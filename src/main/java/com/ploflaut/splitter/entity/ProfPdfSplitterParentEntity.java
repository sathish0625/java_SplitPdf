package com.ploflaut.splitter.entity;

import java.util.List;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "PROF_PDFSPLITTER_PARENT")
public class ProfPdfSplitterParentEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID")
	private int id;
	@Column(name = "DOC_ID",unique = true)
	private String docId;
	@Column(name = "PARENT_DOCNAME")
	private String parentDocName;
	@Column(name = "CREATED_DATE")
	private String createdAt;

	@OneToMany(mappedBy = "parentEntity", cascade = CascadeType.ALL)
	private List<ProfPdfSplitterChildEntity> childEntities;
}
