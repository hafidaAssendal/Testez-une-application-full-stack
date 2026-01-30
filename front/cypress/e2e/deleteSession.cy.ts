describe('Delete Session Component', () => {
  // Données de test
  const mockSession = {
    id: 1,
    name: 'Yoga session',
    description: 'A relaxing yoga session for beginners',
    date: '2026-02-15T00:00:00.000Z',
    teacher_id: 1,
    users: [1, 2, 3],
    createdAt: '2026-01-01T00:00:00.000Z',
    updatedAt: '2026-01-20T00:00:00.000Z'
  };

  const mockTeacher = {
    id: 1,
    firstName: 'Margot',
    lastName: 'Delahaye',
    createdAt: '2025-01-01T00:00:00.000Z',
    updatedAt: '2025-01-01T00:00:00.000Z'
  };

  const adminUser = {
    token: 'admin-token-123',
    type: 'Bearer',
    id: 1,
    username: 'admin@test.com',
    firstName: 'Admin',
    lastName: 'User',
    admin: true
  };

  const mockSessions = [mockSession];

  /**
   * Helper function pour se connecter en tant qu'admin
   */
  const loginAsAdmin = () => {
    cy.visit('/login');
    cy.get('input[formControlName=email]').type(adminUser.username);
    cy.get('input[formControlName=password]').type('test!1234{enter}{enter}');
    cy.wait('@login');
  };

  /**
   * Helper function pour naviguer vers les détails d'une session
   */
  const goToSessionDetail = () => {
    cy.wait('@getSessions');
    cy.contains('button', 'Detail').click();
  };

  beforeEach(() => {
    // Intercepter tous les appels API
    cy.intercept('POST', '/api/auth/login', {
      statusCode: 200,
      body: adminUser
    }).as('login');

    cy.intercept('GET', '/api/session', {
      statusCode: 200,
      body: mockSessions
    }).as('getSessions');

    cy.intercept('GET', '/api/session/1', {
      statusCode: 200,
      body: mockSession
    }).as('getSession');

    cy.intercept('GET', '/api/teacher/1', {
      statusCode: 200,
      body: mockTeacher
    }).as('getTeacher');
  });

  /**
   * TEST: La session est correctement supprimée
   */
  describe('Test - Session deletion', () => {
    it('should successfully delete a session', () => {
      // Intercepter la suppression
      cy.intercept('DELETE', '/api/session/1', {
        statusCode: 200,
        body: {}
      }).as('deleteSession');

      // Se connecter en tant qu'admin
      loginAsAdmin();

      // Naviguer vers les détails de la session
      goToSessionDetail();
      cy.wait('@getSession');
      cy.wait('@getTeacher');

      // Vérifier que le bouton Delete est visible (car admin)
      cy.contains('button', 'Delete').should('be.visible');

      // Vérifier les informations de la session avant suppression
      cy.get('mat-card-title h1').should('contain', 'Yoga Session');

      // Cliquer sur le bouton Delete
      cy.contains('button', 'Delete').click();

      // Vérifier que la requête de suppression a été envoyée
      cy.wait('@deleteSession').then((interception) => {
        expect(interception.request.url).to.include('/api/session/1');
      });

      // Vérifier l'affichage du snackbar de confirmation
      cy.contains('Session deleted !').should('be.visible');

      // Vérifier la redirection vers la liste des sessions
      cy.url().should('include', '/sessions');
      cy.url().should('not.include', '/detail');
    });

    it('should display delete button only for admin users', () => {
      // Se connecter en tant qu'admin
      loginAsAdmin();

      // Naviguer vers les détails
      goToSessionDetail();
      cy.wait('@getSession');
      cy.wait('@getTeacher');

      // Le bouton Delete doit être visible pour l'admin
      cy.contains('button', 'Delete').should('be.visible');

      // Vérifier que le bouton contient l'icône delete
      cy.contains('button', 'Delete')
        .find('mat-icon')
        .should('contain', 'delete');
    });

    it('should show confirmation before deleting', () => {
      cy.intercept('DELETE', '/api/session/1', {
        statusCode: 200,
        body: {}
      }).as('deleteSession');

      loginAsAdmin();
      goToSessionDetail();
      cy.wait('@getSession');

      // Vérifier que la session existe avant suppression
      cy.get('mat-card-title h1').should('be.visible');

      // Cliquer sur Delete
      cy.contains('button', 'Delete').click();

      // La requête DELETE doit avoir été appelée
      cy.wait('@deleteSession');

      // Après suppression, on est redirigé
      cy.url().should('include', '/sessions');
    });
  });

});