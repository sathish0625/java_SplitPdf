package com.ploflaut.splitter.entity;



import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "PROF_PDFSPLITTER_CHILD")
public class ProfPdfSplitterChildEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID")
	private int id;
	@Column(name = "DOC_NAME")
	private String docname;
	@Column(name = "CHILD_DOCID")
	private String childDocId;

	@ManyToOne
	@JoinColumn(name = "DOC_ID", referencedColumnName = "DOC_ID")
	private ProfPdfSplitterParentEntity parentEntity;

}
