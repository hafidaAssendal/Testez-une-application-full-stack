describe('Session Detail Component', () => {
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

  const regularUser = {
    token: 'user-token-456',
    type: 'Bearer',
    id: 2,
    username: 'user@test.com',
    firstName: 'Regular',
    lastName: 'User',
    admin: false
  };

  /**
   * Helper function pour se connecter
   */
  const loginAs = (user: any) => {
    cy.visit('/login');

    cy.get('input[formControlName=email]').type(user.username);
    cy.get('input[formControlName=password]').type('test!1234{enter}{enter}');

    cy.wait('@login');
    cy.url().should('include', '/sessions');
  };

  /**
   * Helper function pour naviguer vers les détails d'une session
   */
  const goToSessionDetail = () => {
    // Attendre que la liste des sessions soit chargée
    cy.wait('@getSessions');

    // Cliquer sur le bouton Detail de la première session
    cy.contains('button', 'Detail').click();
  };

  beforeEach(() => {
    // Intercepter l'appel de login avec une fonction pour retourner l'utilisateur approprié
    cy.intercept('POST', '/api/auth/login', (req) => {
      // Déterminer quel utilisateur retourner basé sur l'email
      const email = req.body.email;
      let user;

      if (email === 'admin@test.com') {
        user = adminUser;
      } else if (email.includes('id=5')) {
        // Pour les tests avec un utilisateur id=5
        user = { ...regularUser, id: 5, username: email };
      } else {
        user = regularUser;
      }

      req.reply({
        statusCode: 200,
        body: user
      });
    }).as('login');

    cy.intercept('GET', '/api/session/1', {
      statusCode: 200,
      body: mockSession
    }).as('getSession');

    cy.intercept('GET', '/api/teacher/1', {
      statusCode: 200,
      body: mockTeacher
    }).as('getTeacher');

    cy.intercept('GET', '/api/session', {
      statusCode: 200,
      body: [mockSession]
    }).as('getSessions');
  });

  /**
   * TEST 1: Vérifier que les informations de la session sont correctement affichées
   */
  describe('Test 1 - Session information display', () => {
    it('should display all session information correctly', () => {
      // Se connecter en tant qu'utilisateur régulier
      loginAs(regularUser);

      // Naviguer vers les détails via le bouton Detail
      goToSessionDetail();

      // Attendre que les données soient chargées
      cy.wait('@getSession');
      cy.wait('@getTeacher');

      // Vérifier le titre de la session (avec titlecase)
      cy.get('mat-card-title h1')
        .should('be.visible')
        .and('contain', 'Yoga Session'); // titlecase transforme la première lettre en majuscule

      // Vérifier les informations du professeur
      cy.get('mat-card-subtitle')
        .should('be.visible')
        .and('contain', mockTeacher.firstName)
        .and('contain', mockTeacher.lastName.toUpperCase());

      // Vérifier l'icône du professeur
      cy.get('mat-card-subtitle mat-icon')
        .should('contain', 'people');

      // Vérifier le nombre de participants
      cy.get('mat-card-content')
        .should('contain', `${mockSession.users.length} attendees`);

      // Vérifier l'icône du groupe
      cy.get('mat-card-content mat-icon')
        .first()
        .should('contain', 'group');

      // Vérifier la date de la session (format longDate)
      cy.get('mat-card-content')
        .should('contain', 'February 15, 2026'); // longDate format

      // Vérifier l'icône du calendrier
      cy.contains('mat-icon', 'calendar_month')
        .should('be.visible');

      // Vérifier la description
      cy.get('.description')
        .should('contain', 'Description:')
        .and('contain', mockSession.description);

      // Vérifier la date de création
      cy.get('.created')
        .should('contain', 'Create at:')
        .and('contain', 'January 1, 2026');

      // Vérifier la date de dernière mise à jour
      cy.get('.updated')
        .should('contain', 'Last update:')
        .and('contain', 'January 20, 2026');

      // Vérifier que l'image de la session est affichée
      cy.get('img.picture')
        .should('be.visible')
        .and('have.attr', 'src', 'assets/sessions.png')
        .and('have.attr', 'alt', 'Yoga session');
    });

    it('should display participate button for non-participating regular user', () => {
      // Utilisateur régulier qui ne participe pas (id=5 n'est pas dans mockSession.users)
      const nonParticipatingUser = {
        ...regularUser,
        id: 5,
        username: 'nonparticipant-id=5@test.com'  // Email spécial pour le mock
      };

      // Se connecter
      loginAs(nonParticipatingUser);

      // Naviguer vers les détails
      goToSessionDetail();
      cy.wait('@getSession');

      // Vérifier que le bouton "Participate" est affiché
      cy.contains('button', 'Participate')
        .should('be.visible');

      // Vérifier l'icône
      cy.contains('button', 'Participate')
        .find('mat-icon')
        .should('contain', 'person_add');
    });

    it('should display unparticipate button for participating regular user', () => {
      // Utilisateur régulier qui participe déjà (id = 2 est dans la liste des users)
      loginAs(regularUser);

      // Naviguer vers les détails
      goToSessionDetail();
      cy.wait('@getSession');

      // Vérifier que le bouton "Do not participate" est affiché
      cy.contains('button', 'Do not participate')
        .should('be.visible');

      // Vérifier l'icône
      cy.contains('button', 'Do not participate')
        .find('mat-icon')
        .should('contain', 'person_remove');
    });
  });

  /**
   * TEST 2: Vérifier que le bouton delete n'apparaît que pour les administrateurs
   */
  describe('Test 2 - Delete button visibility based on user role', () => {
    it('should display delete button when user is admin', () => {
      // Se connecter en tant qu'administrateur
      loginAs(adminUser);

      // Naviguer vers les détails
      goToSessionDetail();
      cy.wait('@getSession');
      cy.wait('@getTeacher');

      // Vérifier que le bouton Delete est visible
      cy.contains('button', 'Delete')
        .should('be.visible');

      // Vérifier l'icône delete
      cy.contains('button', 'Delete')
        .find('mat-icon')
        .should('contain', 'delete');

      // Vérifier que les boutons participate/unparticipate ne sont PAS affichés
      cy.contains('button', 'Participate').should('not.exist');
      cy.contains('button', 'Do not participate').should('not.exist');
    });

    it('should NOT display delete button when user is not admin', () => {
      // Se connecter en tant qu'utilisateur régulier
      loginAs(regularUser);

      // Naviguer vers les détails
      goToSessionDetail();
      cy.wait('@getSession');
      cy.wait('@getTeacher');

      // Vérifier que le bouton Delete n'est PAS visible
      cy.contains('button', 'Delete').should('not.exist');

      // Vérifier qu'au moins un bouton de participation est affiché
      cy.get('button').should('have.length.at.least', 1);
    });

    it('should trigger delete action when admin clicks delete button', () => {
      // Intercepter la requête de suppression
      cy.intercept('DELETE', '/api/session/1', {
        statusCode: 200,
        body: {}
      }).as('deleteSession');

      // Se connecter en tant qu'administrateur
      loginAs(adminUser);

      // Naviguer vers les détails
      goToSessionDetail();
      cy.wait('@getSession');

      // Cliquer sur le bouton Delete
      cy.contains('button', 'Delete').click();

      // Vérifier que la requête de suppression a été envoyée
      cy.wait('@deleteSession');

      // Vérifier que le snackbar de confirmation est affiché
      cy.contains('Session deleted !').should('be.visible');

      // Vérifier la redirection vers /sessions
      cy.url().should('include', '/sessions');
    });
  });


});