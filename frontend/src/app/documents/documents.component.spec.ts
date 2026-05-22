import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { KeycloakService } from 'keycloak-angular';
import { DocumentsComponent } from './documents.component';

describe('DocumentsComponent', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DocumentsComponent],
      providers: [
        provideHttpClient(),
        { provide: KeycloakService, useValue: { getToken: async () => 'tok' } },
      ],
    }).compileComponents();
  });

  it('should create', () => {
    const fixture = TestBed.createComponent(DocumentsComponent);
    expect(fixture.componentInstance).toBeTruthy();
  });

  it('drag enter sets dragOver flag', () => {
    const fixture = TestBed.createComponent(DocumentsComponent);
    const cmp = fixture.componentInstance;
    const evt = new DragEvent('dragover');
    cmp.onDragOver(evt);
    expect(cmp.dragOver()).toBeTrue();
  });
});

