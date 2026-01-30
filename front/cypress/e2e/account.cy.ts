/// <reference types="cypress" />
describe('Account (Me) Component', () => {
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

    const mockUserDetails = {
        id: 2,
        email: 'user@test.com',
        firstName: 'Regular',
        lastName: 'User',
        admin: false,
        password: '',
        createdAt: '2025-12-01T00:00:00.000Z',
        updatedAt: '2026-01-15T00:00:00.000Z'
    };

    const mockAdminDetails = {
        id: 1,
        email: 'admin@test.com',
        firstName: 'Admin',
        lastName: 'User',
        admin: true,
        password: '',
        createdAt: '2025-11-01T00:00:00.000Z',
        updatedAt: '2026-01-10T00:00:00.000Z'
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
    const loginAs = (user: any, userDetails: any) => {
        cy.intercept('POST', '/api/auth/login', {
            statusCode: 200,
            body: user
        }).as('login');

        cy.intercept('GET', `/api/user/${user.id}`, {
            statusCode: 200,
            body: userDetails
        }).as('getUserDetails');

        cy.visit('/login');
        cy.get('input[formControlName=email]').type(user.username);
        cy.get('input[formControlName=password]').type('test!1234{enter}{enter}');
        cy.wait('@login');
    };

    beforeEach(() => {
        cy.intercept('GET', '/api/session', {
            statusCode: 200,
            body: mockSessions
        }).as('getSessions');
    });

    /**
     * TEST: Affichage des informations de l'utilisateur
     */
    describe('Test - Display user information', () => {
        it('should display all user information correctly for regular user', () => {
            // Se connecter en tant qu'utilisateur régulier
            loginAs(regularUser, mockUserDetails);
            cy.wait('@getSessions');

            // Naviguer vers la page Account (Me)
            cy.contains('span', 'Account').click();
            // Vérifier la redirection vers la page de me
            cy.url().should('match', /\/(me)?$/);

            cy.wait('@getUserDetails');

            // Vérifier le titre
            cy.get('h1').should('contain', 'User information');

            // Vérifier le nom complet (lastName en majuscules)
            cy.contains('Name:').parent().should('contain', mockUserDetails.firstName);
            cy.contains('Name:').parent().should('contain', mockUserDetails.lastName.toUpperCase());

            // Vérifier l'email
            cy.contains('Email:').parent().should('contain', mockUserDetails.email);

            // Vérifier que "You are admin" n'est PAS affiché pour un utilisateur régulier
            cy.contains('You are admin').should('not.exist');

            // Vérifier que le bouton de suppression de compte est affiché pour un utilisateur non-admin
            cy.contains('Delete my account:').should('be.visible');
            cy.contains('button', 'Detail').should('be.visible'); // Le bouton avec texte "Detail" existe

            // Vérifier les dates
            cy.contains('Create at:').parent().should('contain', 'December 1, 2025');
            cy.contains('Last update:').parent().should('contain', 'January 15, 2026');
        });

        it('should display admin badge for admin user', () => {
            // Se connecter en tant qu'admin
            loginAs(adminUser, mockAdminDetails);
            cy.wait('@getSessions');
            // Naviguer vers la page Account (Me)
            cy.contains('span', 'Account').click();
            // Vérifier la redirection vers la page de me
            cy.url().should('match', /\/(me)?$/);

            cy.wait('@getUserDetails');

            // Vérifier le titre
            cy.get('h1').should('contain', 'User information');

            // Vérifier les informations de l'admin
            cy.contains('Name:').parent().should('contain', mockAdminDetails.firstName);
            cy.contains('Name:').parent().should('contain', mockAdminDetails.lastName.toUpperCase());
            cy.contains('Email:').parent().should('contain', mockAdminDetails.email);

            // Vérifier que "You are admin" EST affiché
            cy.contains('You are admin').should('be.visible');

            // Vérifier que le bouton de suppression n'est PAS affiché pour un admin
            cy.contains('Delete my account:').should('not.exist');
            cy.get('button').contains('Detail').should('not.exist');

            // Vérifier les dates
            cy.contains('Create at:').parent().should('contain', 'November 1, 2025');
            cy.contains('Last update:').parent().should('contain', 'January 10, 2026');
        });

        it('should display all user data fields', () => {
            loginAs(regularUser, mockUserDetails);
            cy.wait('@getSessions');
            // Naviguer vers la page Account (Me)
            cy.contains('span', 'Account').click();
            // Vérifier la redirection vers la page de me
            cy.url().should('match', /\/(me)?$/);
            cy.wait('@getUserDetails');

            // Vérifier que toutes les sections sont présentes
            cy.contains('Name:').should('be.visible');
            cy.contains('Email:').should('be.visible');
            cy.contains('Create at:').should('be.visible');
            cy.contains('Last update:').should('be.visible');

            // Vérifier la structure mat-card
            cy.get('mat-card').should('be.visible');
            cy.get('mat-card-title').should('be.visible');
            cy.get('mat-card-content').should('be.visible');
        });
    });

    /**
     * Tests de navigation
     */
    describe('Navigation tests', () => {
        it('should navigate back when clicking back button', () => {
            loginAs(regularUser, mockUserDetails);
            cy.wait('@getSessions');
            // Naviguer vers la page Account (Me)
            cy.contains('span', 'Account').click();
            // Vérifier la redirection vers la page de me
            cy.url().should('match', /\/(me)?$/);
            cy.wait('@getUserDetails');

            // Cliquer sur le bouton retour
            cy.get('button[mat-icon-button]').first().click();

            // Vérifier que l'on est retourné à la page précédente
            // (Dans ce cas, probablement /sessions)
            cy.url().should('not.include', '/me');
        });

        it('should be accessible from the main navigation', () => {
            loginAs(regularUser, mockUserDetails);
            cy.wait('@getSessions');

           // Naviguer vers la page Account (Me)
            cy.contains('span', 'Account').click();
            // Vérifier la redirection vers la page de me
            cy.url().should('match', /\/(me)?$/);
            cy.wait('@getUserDetails');

            // Vérifier que la page est bien chargée
            cy.get('h1').should('contain', 'User information');
            cy.url().should('include', '/me');
        });
    });

    /**
     * Tests de suppression de compte
     */
    describe('Account deletion tests', () => {
        it('should display delete button for regular users', () => {
            loginAs(regularUser, mockUserDetails);
            cy.wait('@getSessions');
            // Naviguer vers la page Account (Me)
            cy.contains('span', 'Account').click();
            // Vérifier la redirection vers la page de me
            cy.url().should('match', /\/(me)?$/);
            cy.wait('@getUserDetails');

            // Vérifier que la section de suppression est visible
            cy.contains('Delete my account:').should('be.visible');

            // Vérifier que le bouton de suppression existe
            cy.get('button[color="warn"]').should('be.visible');
            cy.get('button').contains('Detail').should('be.visible');
        });

        it('should NOT display delete button for admin users', () => {
            loginAs(adminUser, mockAdminDetails);
            cy.wait('@getSessions');
            // Naviguer vers la page Account (Me)
            cy.contains('span', 'Account').click();
            // Vérifier la redirection vers la page de me
            cy.url().should('match', /\/(me)?$/);
            cy.wait('@getUserDetails');

            // Vérifier que la section de suppression n'est PAS visible
            cy.contains('Delete my account:').should('not.exist');

            // Vérifier qu'il n'y a pas de bouton de suppression
            cy.get('button').contains('Detail').should('not.exist');
        });

        it('should successfully delete account for regular user', () => {
            // Intercepter la suppression
            cy.intercept('DELETE', `/api/user/${regularUser.id}`, {
                statusCode: 200,
                body: {}
            }).as('deleteUser');

            loginAs(regularUser, mockUserDetails);
            cy.wait('@getSessions');
            // Naviguer vers la page Account (Me)
            cy.contains('span', 'Account').click();
            // Vérifier la redirection vers la page de me
            cy.url().should('match', /\/(me)?$/);
            cy.wait('@getUserDetails');

            // Cliquer sur le bouton de suppression
            cy.get('button[color="warn"]').click();

            // Vérifier que la requête de suppression a été envoyée
            cy.wait('@deleteUser');

            // Vérifier l'affichage du snackbar
            cy.contains('Your account has been deleted !').should('be.visible');

            // Vérifier la redirection vers la page de login
            cy.url().should('not.include', '/me');
            cy.url().should('match', /\/(login)?$/); // Redirigé vers / ou /login
        });
    });


});