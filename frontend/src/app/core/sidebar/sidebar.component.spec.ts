import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { SidebarComponent } from './sidebar.component';

describe('SidebarComponent', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SidebarComponent],
      providers: [provideRouter([])],
    }).compileComponents();
  });

  it('should create', () => {
    const fixture = TestBed.createComponent(SidebarComponent);
    expect(fixture.componentInstance).toBeTruthy();
  });

  it('exposes navigation items', () => {
    const fixture = TestBed.createComponent(SidebarComponent);
    const cmp = fixture.componentInstance;
    expect(cmp.navItems.length).toBeGreaterThan(0);
    expect(cmp.adminItems.find(i => i.id === 'admin')).toBeDefined();
  });
});

