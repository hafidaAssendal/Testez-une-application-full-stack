
/// <reference types="cypress" />

describe('Logout Functionality', () => {
     
  // Données de test
  const regularUser = {
    token: 'user-token-456',
    type: 'Bearer',
    id: 2,
    username: 'user@test.com',
    firstName: 'Regular',
    lastName: 'User',
    admin: false
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

  const mockSessions = [
    {
      id: 1,
      name: 'Yoga session',
      description: 'A relaxing yoga session',
      date: '2026-02-15T00:00:00.000Z',
      teacher_id: 1,
      users: [1, 2, 3],
      createdAt: '2026-01-01T00:00:00.000Z',
      updatedAt: '2026-01-20T00:00:00.000Z'
    }
  ];

  /**
   * Helper function pour se connecter
   */
  const loginAs = (user: any) => {
    cy.intercept('POST', '/api/auth/login', {
      statusCode: 200,
      body: user
    }).as('login');

    cy.intercept('GET', '/api/session', {
      statusCode: 200,
      body: mockSessions
    }).as('getSessions');

  cy.visit('/login');
   cy.get('input[formControlName=email]').type(user.username);
   cy.get('input[formControlName=password]').type('test!1234{enter}{enter}');
   cy.wait('@login');
   cy.url().should('include', '/sessions')
  };

  /**
   * TEST: Déconnexion de l'utilisateur
   */
  describe('Test - User logout', () => {
    it('should successfully logout a regular user', () => {
      // Se connecter
      loginAs(regularUser);
      cy.wait('@getSessions');

      // Vérifier que l'utilisateur est bien connecté
      cy.url().should('include', '/sessions');

      // Vérifier que le bouton de déconnexion est visible dans la toolbar
      // Le bouton Logout devrait être dans mat-toolbar
      cy.get('mat-toolbar').within(() => {
        cy.contains('span', 'Logout').should('be.visible');
      });

      // Cliquer sur le bouton Logout
      cy.contains('span', 'Logout').click();

      // Vérifier la redirection vers la page de login
      cy.url().should('match', /\/(login)?$/);
      
      
      // Attendre que la page de login soit complètement chargée
      cy.get('mat-card', { timeout: 10000 }).should('be.visible');
      // Vérifier que nous sommes sur la page de login
      cy.get('mat-card-title').should('contain', 'Login');
      cy.get('input[formControlName=email]').should('be.visible');
      cy.get('input[formControlName=password]').should('be.visible');
    });

    it('should successfully logout an admin user', () => {
      // Se connecter en tant qu'admin
      loginAs(adminUser);
      cy.wait('@getSessions');

      // Vérifier que l'admin est connecté
      cy.url().should('include', '/sessions');

      // Cliquer sur Logout
      cy.contains('span', 'Logout').click();

      // Vérifier la redirection
      cy.url().should('match', /\/(login)?$/);
      cy.get('mat-card-title').should('contain', 'Login');
    });

    it('should clear session data after logout', () => {
      loginAs(regularUser);
      cy.wait('@getSessions');

      // Déconnexion
      cy.contains('span', 'Logout').click();

      // Vérifier qu'on est redirigé vers login
      cy.url().should('match', /\/(login)?$/);

      // Essayer d'accéder à une page protégée
      cy.visit('/sessions');

      // On devrait être redirigé vers login (grâce au AuthGuard)
      cy.url().should('include', '/login');
    });

    it('should prevent access to protected routes after logout', () => {
      loginAs(regularUser);
      cy.wait('@getSessions');

      // Déconnexion
      cy.contains('span', 'Logout').click();
      cy.url().should('match', /\/(login)?$/);

      // Tenter d'accéder à différentes pages protégées
      cy.visit('/sessions');
      cy.url().should('include', '/login');

      cy.visit('/me');
      cy.url().should('include', '/login');

      // Pour un admin qui tenterait d'accéder aux pages de création
      cy.visit('/sessions/create');
      cy.url().should('include', '/login');
    });

    it('should display logout button only when user is logged in', () => {
      // Visiter la page de login sans être connecté
      cy.visit('/login');

      // Le bouton Logout ne devrait pas être visible 
      cy.get('mat-toolbar').should('exist');
      cy.contains('span', 'Logout').should('not.exist');

      // Se connecter
      loginAs(regularUser);
      cy.wait('@getSessions');

      // Le bouton Logout devrait maintenant être visible
      cy.get('mat-toolbar').should('be.visible');
      cy.contains('span', 'Logout').should('be.visible');
    });
  });


  describe('Security tests', () => {
    it('should not allow access to sessions after logout', () => {
      loginAs(regularUser);
      cy.wait('@getSessions');

      // Vérifier l'accès aux sessions
      cy.url().should('include', '/sessions');
      cy.get('mat-card').should('be.visible');

      // Logout
      cy.contains('span', 'Logout').click();

      // Tenter d'accéder aux sessions
      cy.visit('/sessions');

      // Devrait être redirigé vers login
      cy.url().should('include', '/login');
      cy.get('mat-card-title').should('contain', 'Login');
    });

    it('should clear authentication token after logout', () => {
      loginAs(regularUser);
      cy.wait('@getSessions');

      // Logout
      cy.contains('span', 'Logout').click();

      // Vérifier que les requêtes suivantes n'ont pas de token
      cy.intercept('GET', '/api/session', (req) => {
        // Le header Authorization ne devrait pas être présent
        expect(req.headers.authorization).to.be.undefined;
      }).as('getSessionsWithoutAuth');

      // Tenter d'accéder à une page (sera redirigé)
      cy.visit('/sessions');
      cy.url().should('include', '/login');
    });

    it('should require new authentication after logout', () => {
      loginAs(regularUser);
      cy.wait('@getSessions');

      // Logout
      cy.contains('span', 'Logout').click();
      cy.url().should('match', /\/(login)?$/);

      // Tenter d'accéder à une page protégée sans se reconnecter
      cy.visit('/sessions');
      cy.url().should('include', '/login');

      // Le formulaire de login devrait être affiché
      cy.get('input[formControlName=email]').should('be.visible');
      cy.get('input[formControlName=password]').should('be.visible');
      cy.get('button[type=submit]').should('be.visible');
    });
  });
});